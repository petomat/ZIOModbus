package services.modbusRegisters

import services.modbusRegisters.api.ModbusRegistersService
import services.modbusRegisters.impl.DefaultModbusRegistersService
import services.modbusRequestResponse.impl.DefaultModbusRequestResponseService
import services.modbusRequestResponse.impl.protocol.RWModbusProtocol
import zio.ZIO
import zio.Scope
import zio.Console
import zio.test.TestAspect
import zio.test.TestEnvironment
import zio.test.Spec
import zio.test.check
import zio.test.Gen
import zio.test.Sized
import zio.test.assertTrue
import zio.test.ZIOSpecDefault
import zio.test.defaultTestRunner


object ModbusRegistersSpec extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment with Scope, Any] = {
    suite("Bus Spec")(
//      test("sayHello correctly displays output") {
//        for {
//          _ <- Console.printLine("!")
//          is <- ZIO.succeed(Seq(1,2,3))
//        } yield assertTrue(is == Vector(1, 2, 3))
//      } @@ TestAspect.around(
//        before = Console.printLine("Before"),
//        after = Console.printLine("After").orDie
//      ) @@ TestAspect.scala2Only,
//      test("generating small list of characters") {
//        check(Gen.small(Gen.listOfN(_)(Gen.alphaNumericChar))) { n =>
//          ZIO.attempt(n) *> Sized.size.map(s => assertTrue(s == 100))
//        }
//      } @@ TestAspect.size(100) @@ TestAspect.samples(5)
      test("write") {
        for {
          _ <- ModbusRegistersService.readRegisters(slaveId = 1.toByte, registerOffset = 0.toShort, numberOfRegisters = 1.toShort)
        } yield assertTrue(true)
      }.provide(
        DefaultModbusRegistersService.live,
        // TODO: try  DefaultModbusRequestResponseService.live(RWModbusProtocol),
        DefaultModbusRequestResponseService.live[RWModbusProtocol.Request, RWModbusProtocol.Response],
        TestSerialPortService.layer
      )
    ) @@ TestAspect.timed
  }
}
