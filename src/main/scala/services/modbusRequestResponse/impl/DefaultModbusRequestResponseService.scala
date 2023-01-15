package services.modbusRequestResponse.impl

import zio._
import services.modbusRequestResponse.api.ModbusRequestResponseService
import services.modbusRequestResponse.api.ModbusRequestResponseService.SlaveId
import services.modbusRequestResponse.api.protocol.ModbusRequest
import services.modbusRequestResponse.api.protocol.ModbusResponse
import services.serialPort.api.SerialPortService
import utils.ZIOOps


final class DefaultModbusRequestResponseService[Req <: ModbusRequest[Resp], Resp <: ModbusResponse](
  serialPortService: SerialPortService,
) extends ModbusRequestResponseService[Req, Resp] {
  def request(slaveId: SlaveId, request: Req): ZIO[Any, ModbusRequestResponseService.Error, request.Response] = {
    val rawBytes: Chunk[Byte] = Chunk(slaveId) ++ request.payload
    val allBytes: Chunk[Byte] = rawBytes ++ CRC16(rawBytes).toBytes
    for {
      // send to serial line:
      _ <- serialPortService.writeBytes(allBytes).orElse(ZIO.dieMessage("Could not write bytes")) // TODO: error handling
      _ <- Clock.sleep(200.millis)
      // read response
      responseBytesWithCRC <- serialPortService.readAllAvailableBytesButAtLeastOneByte.debugBy(_.length.toString).orElse(ZIO.dieMessage("Could not read response bytes")) // TODO: error handling
      // TODO: fail if no bytes read or better less than for at least an error response
      responseBytesWithoutCRC <- ZIO.fromOption(CRC16.check(responseBytesWithCRC)).orElseFail(ModbusRequestResponseService.Error.CRC)
      response <- ZIO.fromEither(request.createResponseFromBytes(responseBytesWithoutCRC)).mapError(ModbusRequestResponseService.Error.Modbus)
    } yield response
  }
}

object DefaultModbusRequestResponseService {
  def live[
    Req <: ModbusRequest[Resp] : Tag,
    Resp <: ModbusResponse : Tag
  ]: ZLayer[SerialPortService, Nothing, ModbusRequestResponseService[Req, Resp]] = {
    ZLayer.fromFunction { sps: SerialPortService => new DefaultModbusRequestResponseService[Req, Resp](sps) }
  }
}
