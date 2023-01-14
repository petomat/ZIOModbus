package services.modbusRequestResponse.impl

import zio._
import services.modbusRequestResponse.api.ModbusRequestResponseService
import services.modbusRequestResponse.api.ModbusRequestResponseService.SlaveId
import services.modbusRequestResponse.api.protocol.ModbusProtocol
import services.modbusRequestResponse.api.protocol.ModbusRequest
import services.modbusRequestResponse.api.protocol.ModbusResponse
import services.serialPort.api.SerialPortService


final class ModbusRequestResponseServiceImpl[Req <: ModbusRequest[Resp], Resp <: ModbusResponse](
  serialPortService: SerialPortService,
) extends ModbusRequestResponseService[Req, Resp] {
  def request(slaveId: SlaveId, request: Req): ZIO[Any, ModbusRequestResponseService.Error, request.Response] = {
    for {
      // send to serial line:
      // TODO: slaveId
      _ <- serialPortService.writeBytes(request.payload).orElseSucceed(???)
      // read response (TODO check checksum)
      b <- serialPortService.readAllAvailableBytesButAtLeastOneByte.orElseSucceed(???)
      // TODO: request.responseLength
      r <- ZIO.fromEither(request.createResponseFromBytes(b)).mapError(ModbusRequestResponseService.Error.Modbus)
    } yield r
  }
}

object ModbusRequestResponseServiceImpl {
  def live[
    Protocol <: ModbusProtocol
  ](p: Protocol): ZLayer[SerialPortService, Nothing, ModbusRequestResponseService[p.Request, p.Response]] = {
    live[p.Request, p.Response]
  }
  def live[
    Req <: ModbusRequest[Resp] : Tag,
    Resp <: ModbusResponse : Tag
  ]: ZLayer[SerialPortService, Nothing, ModbusRequestResponseService[Req, Resp]] = {
    ZLayer {
      ZIO.serviceWith[SerialPortService]{
        serialPortService => new ModbusRequestResponseServiceImpl[Req, Resp](serialPortService)
      }
    }
  }
}


