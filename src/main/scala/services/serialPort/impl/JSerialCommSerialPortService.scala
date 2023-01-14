package services.serialPort.impl

import zio.Chunk
import zio.ZIO
import zio.ZLayer
import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortInvalidPortException
import services.serialPort.api.SerialPortService
import zio.Scope
import utils._
import zio.Ref
import zio.Schedule
import zio.Semaphore
import zio.durationInt


// TODO: error type!
final class JSerialCommSerialPortService(
  val port: SerialPort,
  val semaphore: Semaphore
) extends SerialPortService {
  private[this] def enrichError[R, A](zio: ZIO[R, String, A]): ZIO[R, String, A] = {
    zio.mapError { s => s"Serial port '${port.getPortDescription}' : $s" }
  }

  private[this] def read(amount: Int): ZIO[Any, String, Chunk[Byte]] = {
    amount match {
      case n if n < 0 =>
        ZIO.fail("could not read a negative amount of bytes")
      case 0          =>
        ZIO.succeed(Chunk.empty[Byte])
      case a          =>
        // TODO: reuse array for performance
        val arr = new Array[Byte](a)
        for {
          read <- ZIO.succeedBlocking(port.readBytes(arr, a))
          bs <- read match {
            case -1  => ZIO.fail("There was an error reading from the port")
            case `a` => ZIO.succeed(Chunk.fromArray(arr))
            case n   => ZIO.succeed(Chunk.fromArray(arr).take(n))
          }
        } yield bs
    }
  }


  def readBytes(amount: Int): ZIO[Any, String, Chunk[Byte]] = {
    semaphore.withPermit(enrichError(read(amount)))
  }

  val readAllAvailableBytesButAtLeastOneByte: ZIO[Any, String, Chunk[Byte]] = {
    semaphore.withPermit {
      enrichError {
        for {
          available <- ZIO.succeedBlocking(port.bytesAvailable())
          bytes <- available match {
            case -1 =>
              ZIO.fail("port should be open when reading")
            case a  =>
              read(a)
          }
        } yield bytes
      }
    }
  }

  def writeBytes(bytes: Chunk[Byte]): ZIO[Any, String, Unit] = {
    semaphore.withPermit {
      enrichError {
        val len = bytes.length
        for {
          // TODO: blocking buffering and so on, see https://github.com/Fazecast/jSerialComm/wiki/Blocking-and-Semiblocking-Reading-Usage-Example
          // TODO: ZIO.attemptBlockingInterrupt or alike
          written <- ZIO.succeedBlocking(port.writeBytes(bytes.toArray, len))
          _ <- written match {
            case `len`        => ZIO.unit
            case -1           => ZIO.fail("There was an error writing to the port") // TODO: end of stream
            case n if n > len => ZIO.dieMessage("More bytes were unexpectedly written than available") // TODO: according to docu not possible
            case n            => ZIO.fail(s"Only $n of $len bytes were written") // TODO: write remaining...
          }
          // TODO:  ZIO.attemptBlockingInterrupt or alike
          _ <- ZIO.succeedBlocking(port.flushIOBuffers()).filterOrFail(identity)("Could not flush written bytes")
        } yield ()
      }
    }
  }
}


object JSerialCommSerialPortService {
  def scoped(port: String): ZIO[Scope, RuntimeException, SerialPortService] = {
    val acquire: ZIO[Any, RuntimeException, JSerialCommSerialPortService] = {
      // TODO: ZIO.attemptBlockingInterrupt or alike
      ZIO.blocking {
        for {
          p <- ZIO.attempt(SerialPort.getCommPort(port)).refineToOrDie[SerialPortInvalidPortException]
          _ <- ZIO.succeed(
            p.openPort(/*safetySleepTime =*/ 0, /*deviceSendQueueSize =*/ 0, /*deviceReceiveQueueSize =*/ 0)
          ).flatMap {
            case true  => ZIO.unit
            case false => ZIO.fail(
              new IllegalStateException("Could not successfully open the port with a valid configuration.")
            )
          }
          _ <- ZIO.succeed(p.setComPortParameters(115200, 8, 2, 0))
          // TODO: flush stale data?!! ugly:
          a = new Array[Byte](512)
          _ <- ZIO.attempt(p.readBytes(a, 512, 0)).debug.orDie
          s <- Semaphore.make(permits = 1)
        } yield {
          new JSerialCommSerialPortService(p, s)
        }
      }
    }
    val release: JSerialCommSerialPortService => ZIO[Any, Nothing, Unit] = { service =>
      ZIO.succeedBlocking(service.port.closePort())
        .flatMap {
          case true  => ZIO.unit
          case false => ZIO.fail(())
        }
        .retry(Schedule.spaced(100.millis) && Schedule.recurs(3))
        // TODO: where is the log message feeded to?
        .orElseSucceed[Unit](ZIO.logError("Could not close port")) // TODO: why the heck is the Unit type annotation needed?
    }
    ZIO.acquireRelease(
      acquire.debugBy { s => s"OPENED ${s.port} (${s.port.getSystemPortName})" }
    )(
      s => release(s).debugBy { _ => s"CLOSED ${s.port} (${s.port.getSystemPortName})" } // TODO: debug only success since error is logged anyway
    )
  }
  // TODO: port via config?
  def live(port: String): ZLayer[Any, RuntimeException, SerialPortService] = ZLayer.scoped(scoped(port))
}
