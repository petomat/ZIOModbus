import zio.ZIO
import zio.ZIOAppDefault
import zio.ZIOAppArgs
import zio.Scope
import zio.Console
import services.modbusRegisters.api.ModbusRegistersService
import services.modbusRegisters.impl.DefaultModbusRegistersService
import services.modbusRequestResponse.impl.DefaultModbusRequestResponseService
import services.modbusRequestResponse.impl.protocol.RWModbusProtocol
import services.serialPort.impl.JSerialCommSerialPortService


object Main extends ZIOAppDefault {
  // TODO: Error types
  private[this] val app: ZIO[ModbusRegistersService, String, Unit] = {
    for {
      _ <- Console.printLine("---Start---").orDie
      _ <- ModbusRegistersService.readRegisters(slaveId = 1, registerOffset = 0, numberOfRegisters = 4)
      _ <- Console.printLine("---End---").orDie
    } yield ()
  }
  // hint: socat -d -d pty,raw,crnl,echo=0,link=./VirtualSerialPort1 pty,raw,crnl,echo=0,link=./VirtualSerialPort2
  val run: ZIO[ZIOAppArgs with Scope, Any, Any] = {
    app.provide(
      DefaultModbusRegistersService.live,
      DefaultModbusRequestResponseService.live[RWModbusProtocol.Request, RWModbusProtocol.Response],
      JSerialCommSerialPortService.live(port = "/dev/tty.usbserial-CT2IVBPT")
    )
  }
}










