import zio.Cause
import zio.ZIO

package object utils {
  // TODO: wieso geht das mit einem assignment in der zeile danach aber nicht direkt inline?
  implicit final class ZIOOps[R, E, A](private val zio: ZIO[R, E, A]) extends AnyVal {

    // equivalent to filterOrDie(p)(new NoSuchElementException())?
    def withFilter(p: A => Boolean): ZIO[R, E, A] =
      zio.flatMap {
        case a if p(a) => ZIO.succeed(a)
        case _         => ZIO.die(new NoSuchElementException())
      }
    def debugBy(f: A => String, g: Cause[E] => String = _.toString): ZIO[R, E, A] =
      zio
        .tap(value => ZIO.succeed(println(f(value))))
        .tapErrorCause(error => ZIO.succeed(println(s"<FAIL> ${g(error)}")))
  }
}
