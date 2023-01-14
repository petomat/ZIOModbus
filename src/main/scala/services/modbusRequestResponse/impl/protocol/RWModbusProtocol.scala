package services.modbusRequestResponse.impl.protocol

import zio.Chunk
import services.modbusRequestResponse.api.protocol.ModbusProtocol
import services.modbusRequestResponse.api.protocol.ModbusRequest
import services.modbusRequestResponse.api.protocol.ModbusResponse


case object RWModbusProtocol extends ModbusProtocol {
  sealed trait Request extends ModbusRequest[Response]
  object Request {
    case class ReadSingleRegister() extends Request {
      type Response = Response.ReadSingleRegister
      def functionCode: Byte = ???
      def payload: Chunk[Byte] = Chunk.empty
      def createResponseFromBytes(bytes: Chunk[Byte]): Either[ModbusResponse.Exception, Response] = {
        Right(Response.ReadSingleRegister)
      }
      def responseLength = 0
    }
  }
  sealed trait Response extends ModbusResponse
  object Response {
    sealed trait ReadSingleRegister extends Response
    case object ReadSingleRegister extends ReadSingleRegister
  }
}