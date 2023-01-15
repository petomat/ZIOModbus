package services.modbusRegisters.api

import zio.Chunk
import zio.ZIO
import ModbusRegistersService.Register
import services.modbusRequestResponse.api.ModbusRequestResponseService.NumberOfRegisters
import services.modbusRequestResponse.api.ModbusRequestResponseService.RegisterAddress
import services.modbusRequestResponse.api.ModbusRequestResponseService.SlaveId
import services.modbusRequestResponse.api.protocol.ModbusMessageConstants


// Idee: Schreiben/Lesen auf "grossem" Register(=2-Bytes) Array
trait ModbusRegistersService {
  // TODO: error type, z.b. weniger register geschrieben als gewünscht.
  def readRegisters(slaveId: SlaveId, registerOffset: RegisterAddress, numberOfRegisters: NumberOfRegisters): ZIO[Any, String, Chunk[Register]]
  // TODO: wo sicherstellen, dass nur maximal (256-1-1-2)/2=126 register bei modbus möglich sind.
  //       https://www.modbus.org/docs/Modbus_Application_Protocol_V1_1b3.pdf (Page 5)
  def writeRegisters(slaveId: SlaveId, registerOffset: RegisterAddress, registers: Chunk[Register]): ZIO[Any, String, Unit]
}


object ModbusRegistersService {
  case class Register private (twoBytes: Chunk[Byte]) extends AnyVal
  object Register {
    def fromBytes(chunk: Chunk[Byte]): Register = {
      assert(chunk.length == ModbusMessageConstants.ByteLengths.register, "Unexpected register bytes")
      Register(chunk)
    }
  }

  // Accessors:
  def readRegisters(
    slaveId: SlaveId,
    registerOffset: RegisterAddress,
    numberOfRegisters: NumberOfRegisters
  ): ZIO[ModbusRegistersService, String, Chunk[Register]] = {
    ZIO.serviceWithZIO[ModbusRegistersService](_.readRegisters(slaveId, registerOffset, numberOfRegisters))
  }
  def writeRegisters(
    slaveId: SlaveId,
    registerOffset: RegisterAddress,
    registers: Chunk[Register]
  ): ZIO[ModbusRegistersService, String, Unit] = {
    ZIO.serviceWithZIO[ModbusRegistersService](_.writeRegisters(slaveId, registerOffset, registers))
  }
}
