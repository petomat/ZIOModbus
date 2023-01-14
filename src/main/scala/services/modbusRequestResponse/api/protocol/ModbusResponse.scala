package services.modbusRequestResponse.api.protocol

trait ModbusResponse

object ModbusResponse {
  sealed trait Exception extends Product with Serializable
  // Same for all modbus protocols:
  object Exception {
    // TODO: see com.dasgip.controller.modbus.api.ExceptionCode
    case object IllegalFunction extends Exception
    case object IllegalDataAddress extends Exception
    case object IllegalDataValue extends Exception
    case object SlaveDeviceFailure extends Exception
    // ...
  }
}
