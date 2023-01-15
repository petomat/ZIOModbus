package services.modbusRequestResponse.impl.protocol

import services.modbusRegisters.api.ModbusRegistersService.Register
import zio.Chunk
import services.modbusRequestResponse.api.protocol.ModbusProtocol
import services.modbusRequestResponse.api.protocol.ModbusRequest
import services.modbusRequestResponse.api.protocol.ModbusResponse
import services.modbusRequestResponse.api.ModbusRequestResponseService.RegisterAddress
import services.modbusRequestResponse.api.ModbusRequestResponseService.NumberOfRegisters
import services.modbusRequestResponse.api.protocol.ModbusMessageConstants.ByteLengths

case object RWModbusProtocol extends ModbusProtocol {
  sealed trait Request extends ModbusRequest[Response]
  object Request {
    case class ReadHoldingRegisters(registerOffset: RegisterAddress, numberOfRegisters: NumberOfRegisters) extends Request {
      type Response = Response.ReadHoldingRegisters
      def functionCode: Byte = 0x03
      def requestData: Chunk[Byte] = {
        def shortToBytes(value: Short): Chunk[Byte] = {
          Chunk(((value >>> 8) & 0xFF).toByte, (value & 0xFF).toByte)
        }
        // Modbus specification reserves two bytes for register lengths
        shortToBytes(registerOffset) ++ shortToBytes(numberOfRegisters)
      }
      // bytes are without crc
      def createResponseFromBytes(bytes: Chunk[Byte]): Either[ModbusResponse.Exception, Response] = {
        if (bytes.length != responseLength) {
          Left(ModbusResponse.Exception.Dummy(s"Unexpected response length ${bytes.length}, expected $responseLength"))
        } else {
          val (numberOfRegistersBytes, registerBytes) = bytes.splitAt(ByteLengths.numberOfRegisterBytes)
          val n: Int = {
            assert(ByteLengths.numberOfRegisterBytes == 1)
            numberOfRegistersBytes.head.toInt
          }
          // TODO: if not needed because of above if?!!
          if (n * ByteLengths.register != registerBytes.length) {
            Left(ModbusResponse.Exception.Dummy(s"Unexpected register byte length ${registerBytes.length}, expected ${n * ByteLengths.register}"))
          } else {
            val registers: Chunk[Register] = {
              Chunk.fromIterator(registerBytes.grouped(ByteLengths.register).map(Register.fromBytes))
            }
            Right(Response.ReadHoldingRegisters(registers))
          }
        }
      }
      // without crc
      val responseLength: Int = {
        ByteLengths.functionOrErrorCode + ByteLengths.numberOfRegisterBytes + numberOfRegisters * ByteLengths.register
      }
    }
  }
  sealed trait Response extends ModbusResponse
  object Response {
    case class ReadHoldingRegisters(registers: Chunk[Register]) extends Response
  }
}