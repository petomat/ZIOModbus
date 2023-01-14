package services.modbusRegisters.impl

import zio.Chunk
import zio.ZIO
import zio.ZLayer
import services.modbusRegisters.api.ModbusRegistersService
import services.modbusRegisters.api.ModbusRegistersService.Register
import services.modbusRequestResponse.api.ModbusRequestResponseService
import services.modbusRequestResponse.api.ModbusRequestResponseService.NumberOfRegisters
import services.modbusRequestResponse.api.ModbusRequestResponseService.RegisterAddress
import services.modbusRequestResponse.api.ModbusRequestResponseService.SlaveId
import services.modbusRequestResponse.impl.protocol.RWModbusProtocol


final class DefaultModbusRegistersService(
  regRespService: ModbusRequestResponseService[RWModbusProtocol.Request, RWModbusProtocol.Response]
) extends ModbusRegistersService {
  def readRegisters(
    slaveId: SlaveId,
    registerOffset: RegisterAddress,
    numberOfRegisters: NumberOfRegisters
  ): ZIO[Any, String, Chunk[Register]] = {
    for {
      // TODO: error handling!
      r <- regRespService.request(slaveId, RWModbusProtocol.Request.ReadHoldingRegisters(registerOffset, numberOfRegisters)).mapError(_.toString)
    } yield ???
  }
  def writeRegisters(
    slaveId: SlaveId,
    registerOffset: RegisterAddress,
    registers: Chunk[Register]
  ): ZIO[Any, String, Unit] = {
    ???
  }
}


object DefaultModbusRegistersService {
  val live: ZLayer[
    ModbusRequestResponseService[RWModbusProtocol.Request, RWModbusProtocol.Response],
    Nothing,
    DefaultModbusRegistersService
  ] = {
    ZLayer.fromFunction { regRespService => new DefaultModbusRegistersService(regRespService) }
  }
}
