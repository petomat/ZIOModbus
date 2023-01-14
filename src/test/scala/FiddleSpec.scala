import zio._
import zio.test._


object FiddleSpec extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment with Scope, Any] = {
    suite("Bus Spec")(
      test("sayHello correctly displays output") {
        for {
          _ <- Console.printLine("!")
          is <- ZIO.succeed(Seq(1,2,3))
        } yield assertTrue(is == Vector(1, 2, 3))
      } @@ TestAspect.around(
        before = Console.printLine("Before"),
        after = Console.printLine("After").orDie
      ) @@ TestAspect.scala2Only,
      test("generating small list of characters") {
        check(Gen.small(Gen.listOfN(_)(Gen.alphaNumericChar))) { n =>
          ZIO.attempt(n) *> Sized.size.map(s => assertTrue(s == 100))
        }
      } @@ TestAspect.size(100) @@ TestAspect.samples(5)
    ) @@ TestAspect.timed
  }
}

