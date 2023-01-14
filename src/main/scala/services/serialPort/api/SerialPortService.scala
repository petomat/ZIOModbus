package services.serialPort.api

import zio.Chunk
import zio.ZIO

trait SerialPortService {
  // TODO: better name, more scaladoc, if stream closed than error case
  def readAllAvailableBytesButAtLeastOneByte: ZIO[Any, String, Chunk[Byte]]
  def writeBytes(bytes: Chunk[Byte]): ZIO[Any, String, Unit]
}


object SerialPortService {
  def readAllAvailableBytesButAtLeastOneByte: ZIO[SerialPortService, String, Chunk[Byte]] = {
    ZIO.serviceWithZIO[SerialPortService](_.readAllAvailableBytesButAtLeastOneByte)
  }
  def writeBytes(bytes: Chunk[Byte]): ZIO[SerialPortService, String, Unit] = {
    ZIO.serviceWithZIO[SerialPortService](_.writeBytes(bytes))
  }
}
