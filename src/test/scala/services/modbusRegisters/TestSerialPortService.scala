package services.modbusRegisters

import zio.Chunk
import zio.Ref
import zio.ZIO
import zio.ZLayer
import services.serialPort.api.SerialPortService


case class TestSerialPortService(writtenBytes: Ref[Chunk[Byte]]) extends SerialPortService {
  val readAllAvailableBytesButAtLeastOneByte: ZIO[Any, String, Chunk[Byte]] =
  def writeBytes(bytes: Chunk[Byte]): ZIO[Any, String, Unit] = writtenBytes.update(_ ++ bytes)
}

object TestSerialPortService {
  def layer = {
    ZLayer {
      for {
        writtenBytes <- Ref.make(Chunk.empty[Byte])
      } yield TestSerialPortService(writtenBytes)
    }
  }
}
