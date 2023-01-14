package services.modbusRequestResponse.api.protocol

trait ModbusProtocol {
  // Note: These types here will straight forwardly be implemented by sealed traits, so no type members in the implementation
  type Request <: ModbusRequest[Response]
  type Response <: ModbusResponse
}

object ModbusProtocol {
  type Aux[Req, Resp] = ModbusProtocol { type Request = Req ; type Response = Resp }
}


/*
Template for implementation:

case object SomeModbusProtocol extends ModbusProtocol {
  sealed trait Request extends ModbusRequest[Response]
  object Request {
    case object Dummy extends Request {
      type Response = Response.Dummy
      def functionCode: Byte
      def payload: Chunk[Byte]
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
 */