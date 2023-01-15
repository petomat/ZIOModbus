package services.modbusRequestResponse.impl.protocol

import zio.Chunk
import services.modbusRequestResponse.api.protocol.ModbusProtocol
import services.modbusRequestResponse.api.protocol.ModbusRequest
import services.modbusRequestResponse.api.protocol.ModbusResponse


// TODO: just dummy here, to implement

case object FirmwareUpdateModbusProtocol extends ModbusProtocol {
  sealed trait Request extends ModbusRequest[Response]
  object Request {
    case object Dummy extends Request {
      type Response = Response.Dummy
      def functionCode: Byte = ???
      def requestData: Chunk[Byte] = Chunk.empty
      def createResponseFromBytes(bytes: Chunk[Byte]): Either[ModbusResponse.Exception, Response] = {
        Right(Response.Dummy)
      }
      def responseLength: Int = 0
    }
  }
  sealed trait Response extends ModbusResponse
  object Response {
    sealed trait Dummy extends Response
    case object Dummy extends Dummy
  }
}