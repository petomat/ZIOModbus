package services.modbusRegistersService.api

import zio.Chunk
import zio.ZIO
import ModbusRegistersService.Register
import services.modbusRequestResponse.api.ModbusRequestResponseService.SlaveId


// Idee: Schreiben/Lesen auf "grossem" Register(=2-Bytes) Array
trait ModbusRegistersService {
  // TODO: error type, z.b. weniger register geschrieben als gewünscht.
  def readRegisters(slaveId: SlaveId, registerOffset: Int, amountOfRegisters: Int): ZIO[Any, String, Chunk[Register]]
  // TODO: wo sicherstellen, dass nur maximal (256-1-1-2)/2=126 register bei modbus möglich sind.
  //       https://modbus.org/docs/Modbus_Application_Protocol_V1_1b.pdf (Page 5)
  def writeRegisters(slaveId: SlaveId, registerOffset: Int, registers: Chunk[Register]): ZIO[Any, String, Unit]
}


object ModbusRegistersService {
  type Register = String
  // Accessors:
  def readRegisters(
    slaveId: SlaveId,
    registerOffset: Int,
    amountOfRegisters: Int
  ): ZIO[ModbusRegistersService, String, Chunk[Register]] = {
    ZIO.serviceWithZIO[ModbusRegistersService](_.readRegisters(slaveId, registerOffset, amountOfRegisters))
  }
  def writeRegisters(
    slaveId: SlaveId,
    registerOffset: Int,
    registers: Chunk[Register]
  ): ZIO[ModbusRegistersService, String, Unit] = {
    ZIO.serviceWithZIO[ModbusRegistersService](_.writeRegisters(slaveId, registerOffset, registers))
  }
}
