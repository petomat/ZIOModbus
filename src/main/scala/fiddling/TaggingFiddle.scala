package fiddling

import fiddling.TaggingFiddle.@@
import fiddling.TaggingFiddle.TagWith
import zio.Tag
import zio.ZIO
import zio.ZLayer

object TaggingFiddle {
  type Tagged[T] = { type Tag = T }
  type @@[+T, Tag] = T with Tagged[Tag]

  object TagWith {
    // def apply[A, T](a: A): A @@ T = a.asInstanceOf[A @@ T]
    final class PartiallyAppliedTag[T](private val dummy: Boolean = true) extends AnyVal {
      def apply[A](a: A): A @@ T = a.asInstanceOf[A @@ T]
    }
    def apply[T] = new PartiallyAppliedTag[T]
  }

//  case class Foo()
//
//  sealed trait Tag extends Serializable with Product
//  object Tag {
//    sealed trait A extends Tag
//    case object A extends A
//    sealed trait B extends Tag
//    case object B extends B
//  }
//
//
//  val ta: Foo @@ Tag.A = TagWith[Tag.A](Foo())
//  val t: Foo = ta
//  // DOES NOT COMPILE:  val tb: Foo @@ Tag.B = ta
}



trait UbiquitousService {
  val op: ZIO[Any, Nothing, Unit]
}

object UbiquitousService {
  val op: ZIO[UbiquitousService, Nothing, Unit] = ZIO.serviceWith[UbiquitousService](_.op)
}


final class UbiquitousServiceImpl() extends UbiquitousService {
  val op: ZIO[Any, Nothing, Unit] = ZIO.unit
}
object UbiquitousServiceImpl {
  implicit def tagtagtag[A, T](implicit t: Tag[A]): Tag[A @@ T] = t.asInstanceOf[Tag[A @@ T]]
  def live[T]: ZLayer[Any, Nothing, UbiquitousServiceImpl @@ T] = ZLayer.succeed(TagWith[T](new UbiquitousServiceImpl))
}



