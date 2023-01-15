package services.modbusRequestResponse.api.protocol

import zio.Chunk

// R = Upper bound for all responses of the protocol that is the parent trait extending ModbusResponse
// TODO: variance needed?
trait ModbusRequest[R <: ModbusResponse] {
  type Response <: R
  def functionCode: Byte
  def requestData: Chunk[Byte]
  // assert(requestData.length <= 256 - 1 - 1 - 2) see https://modbus.org/docs/Modbus_Application_Protocol_V1_1b.pdf (Page 5)
  final def payload: Chunk[Byte] = Chunk(functionCode) ++ requestData
  def createResponseFromBytes(bytes: Chunk[Byte]): Either[ModbusResponse.Exception, Response]
  // TODO: ever needed?
  def responseLength: Int
}
