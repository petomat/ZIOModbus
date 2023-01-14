import zio.ZIO
import zio.ZIOAppDefault
import zio.ZIOAppArgs
import zio.Scope
import zio.Console
import services.modbusRegistersService.api.ModbusRegistersService
import services.modbusRegistersService.impl.ModbusRegistersServiceImpl
import services.modbusRequestResponse.impl.ModbusRequestResponseServiceImpl
import services.modbusRequestResponse.impl.protocol.RWModbusProtocol
import services.serialPort.impl.JSerialCommSerialPortService


object Main extends ZIOAppDefault {
  // TODO: Error types
  private[this] val app: ZIO[ModbusRegistersService, String, Unit] = {
    for {
      _ <- Console.printLine("---Start---").orDie
      _ <- ModbusRegistersService.readRegisters(slaveId = 1, registerOffset = 0, amountOfRegisters = 1)
      _ <- Console.printLine("---End---").orDie
    } yield ()
  }
  private[this] val modbusProtocol = RWModbusProtocol
  // hint: socat -d -d pty,raw,crnl,echo=0,link=./VirtualSerialPort1 pty,raw,crnl,echo=0,link=./VirtualSerialPort2
  val run: ZIO[ZIOAppArgs with Scope, Any, Any] = {
    app.provide(
      ModbusRegistersServiceImpl.live,
      ModbusRequestResponseServiceImpl.live(modbusProtocol),
      JSerialCommSerialPortService.live(port = "/dev/ttys013")
    )
  }
}













