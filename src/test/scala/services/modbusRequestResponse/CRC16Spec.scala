package services.modbusRequestResponse

import services.modbusRequestResponse.impl.CRC16
import zio.Chunk
import zio.Random
import zio.Scope
import zio.ZIO
import zio.test.Gen
import zio.test.Spec
import zio.test.TestAspect
import zio.test.TestEnvironment
import zio.test.ZIOSpecDefault
import zio.test.assertTrue
import zio.test.check

object CRC16Spec extends ZIOSpecDefault {

  private val r = new scala.util.Random

  def spec: Spec[TestEnvironment with Scope, Any] =
    suite("CRC16 Spec")(
      test("boot up") {
        for {
          result <- ZIO.succeed(CRC16(Chunk(0x01, 0x23, 0x45, 0x67, 0x89, 0xab, 0xcd, 0xef).map(_.toByte)))
        } yield assertTrue(result.toBytes == Chunk(0xe6, 0xf8).map(_.toByte))
      },
      test("example computed with copla") {
        for {
          result <- ZIO.succeed(CRC16(Chunk(0x00, 0x03, 0x00, 0x00, 0x00, 0x0a).map(_.toByte)))
        } yield assertTrue(result.toBytes == Chunk(0xc4, 0x1c).map(_.toByte))
      },
      test("computing and checking CRC are consistent") {
        // this gen is soooo sloooooow
        check(Gen.listOfBounded(5, 254)(Gen.byte)) { list =>
          for {
            array  <- ZIO.succeed(list.toArray)
            chunk  <- ZIO.succeed(Chunk.fromArray(array))
            crc    <- ZIO.succeed(CRC16(chunk))
            result <- ZIO.succeed(CRC16.check(chunk ++ crc.toBytes))
            others <- ZIO.succeed(for {
                        low     <- 0 to 255
                        high    <- 0 to 255
                        wrongCRC = Chunk(low.toByte, high.toByte)
                        if wrongCRC != crc.toBytes
                      } yield { CRC16.check(chunk ++ wrongCRC) })
          } yield assertTrue(result.contains(chunk) && others.forall(_.isEmpty))
        }
      } @@ TestAspect.samples(5),
      test("gen byte is quite a bit faster than in combination with list above...") {
        check(Gen.byte) { _ =>
          for {
            _ <- ZIO.succeed(())
          } yield assertTrue(true)
        }
      } @@ TestAspect.samples(10000)
    ) @@ TestAspect.sequential @@ TestAspect.timed
}
