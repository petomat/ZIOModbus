package services.modbusRegistersService.impl

import zio.Chunk
import zio.ZIO
import zio.ZLayer
import services.modbusRegistersService.api.ModbusRegistersService
import services.modbusRegistersService.api.ModbusRegistersService.Register
import services.modbusRequestResponse.api.ModbusRequestResponseService
import services.modbusRequestResponse.api.ModbusRequestResponseService.SlaveId
import services.modbusRequestResponse.impl.protocol.RWModbusProtocol


final class ModbusRegistersServiceImpl(
  regRespService: ModbusRequestResponseService[RWModbusProtocol.Request, RWModbusProtocol.Response]
) extends ModbusRegistersService {
  def readRegisters(
    slaveId: SlaveId,
    registerOffset: Int,
    amountOfRegisters: Int
  ): ZIO[Any, String, Chunk[Register]] = {
    regRespService.request(slaveId, ???)
    ???
  }
  def writeRegisters(
    slaveId: SlaveId,
    registerOffset: Int,
    registers: Chunk[Register]
  ): ZIO[Any, String, Unit] = {
    ???
  }
}


object ModbusRegistersServiceImpl {
  val live: ZLayer[
    ModbusRequestResponseService[RWModbusProtocol.Request, RWModbusProtocol.Response],
    Nothing,
    ModbusRegistersServiceImpl
  ] = {
    ZLayer.fromFunction { regRespService => new ModbusRegistersServiceImpl(regRespService) }
  }
}
