package services.modbusRequestResponse.api

import services.modbusRequestResponse.api.ModbusRequestResponseService.SlaveId
import zio.ZIO
import zio.Tag
import services.modbusRequestResponse.api.protocol.ModbusRequest
import services.modbusRequestResponse.api.protocol.ModbusResponse


trait ModbusRequestResponseService[Req <: ModbusRequest[Resp], Resp <: ModbusResponse] {
  // Envelope: Who = slaveId, What = request
  def request(slaveId: SlaveId, request: Req): ZIO[Any, ModbusRequestResponseService.Error, request.Response]
}

object ModbusRequestResponseService {
  // TODO: proper types, i.e. case classes
  type BusId = String
  type SlaveId = Int
  // same for all protocols:
  sealed trait Error extends Product with Serializable
  object Error {
    case class Modbus(error: ModbusResponse.Exception) extends Error
    case object CRC extends Error
    case object PayloadExceeded extends Error
  }
  // Accessors:
  def request[Req <: ModbusRequest[Resp] : Tag, Resp <: ModbusResponse : Tag](
    slaveId: SlaveId, request: Req
  ): ZIO[ModbusRequestResponseService[Req, Resp], ModbusRequestResponseService.Error, request.Response] = {
    ZIO.serviceWithZIO[ModbusRequestResponseService[Req, Resp]](_.request(slaveId, request))
  }
}
