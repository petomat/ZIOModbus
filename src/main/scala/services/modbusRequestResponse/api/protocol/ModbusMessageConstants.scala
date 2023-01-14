package services.modbusRequestResponse.api.protocol

object ModbusMessageConstants {
  // Serialized bytes between a modbus master and slave are constructed like:
  // Part name:        SlaveId, FunctionOrErrorCode, Payload, CRC
  // Part byte length:    1   ,          1         ,  <=252 ,  2
  // So, we can transmit up to 252/2=126 registers
  object ByteLengths {
    val slaveId: Int = 1
    val functionOrErrorCode: Int = 1
    val crcLength: Int = 2
    val maxSerialBuffer: Int = 256
    val maxPayload: Int = maxSerialBuffer - slaveId - functionOrErrorCode - crcLength
    val register: Int = 2
    val numberOfRegisterBytes: Int = 1
  }
  val maxPayloadRegisters: Int = ByteLengths.maxPayload / ByteLengths.register
}
