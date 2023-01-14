package services

import zio.Tag
import zio.ZIO
import zio.ZLayer

object Fiddling {
  case class Wrap(value: String)
  val eins: "1" = "1"
  def f[A, B](implicit ev: A =:= B) = ???
  f["1", "1"]
  def singleton[T <: Singleton](t: T): T = t
  val s1: "1" = singleton("1")
  val w1: Wrap = Wrap("A")
  val w1x = valueOf[w1.type]
  val w1y: w1.type = w1x
  val w1z: w1.type = singleton(w1)
}


// T = SingletonTag
trait MultiServiceFiddling[T <: Singleton] {
  val op: ZIO[Any, Nothing, String]
}

object MultiServiceFiddling {
  def op[T <: Singleton : Tag]: ZIO[MultiServiceFiddling[T], Nothing, String] = ZIO.serviceWithZIO[MultiServiceFiddling[T]](_.op)
}

case class Bus(value: String)

final class MultiServiceFiddlingImpl[B <: Bus with Singleton](bus: B) extends MultiServiceFiddling[B] {
  val op: ZIO[Any, Nothing, String] = ZIO.succeed(bus.value)
}

object MultiServiceFiddlingImpl {
  def live[B <: Bus with Singleton : Tag](bus: B): ZLayer[Any, Nothing, MultiServiceFiddlingImpl[B]] = {
    ZLayer.succeed(new MultiServiceFiddlingImpl[B](bus))
  }
}



object Usage {
  val bus1: Bus = Bus("1")
  val bus2: Bus = Bus("2")
  val app: ZIO[MultiServiceFiddling[bus1.type] with MultiServiceFiddling[bus2.type], Nothing, Unit] = {
    for {
      _ <- MultiServiceFiddling.op[bus1.type]
      _ <- MultiServiceFiddling.op[bus2.type]
    } yield ()
  }
  app.provide(
    MultiServiceFiddlingImpl.live(bus1),
    MultiServiceFiddlingImpl.live(bus2)
  )
}