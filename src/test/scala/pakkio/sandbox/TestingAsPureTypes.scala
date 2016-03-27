package pakkio.sandbox

import org.scalatest.FunSuite

class TestingAsPureTypes
  extends FunSuite
    with Testable
    with CheckMemory {

  override val label: String = "TestingAsPureTypes"


  import SetsAsPureTypes._

  test("singleton is working") {
    val s = SingletonSet(4)
    s(4) === true
    s(1) === false
  }

  test("union") {
    val u = Union(s4, s5)

    u(4) === true
    u(5) === true
    u(0) === false

  }
  test("intersect") {


    val i = Intersect(s123, s345)
    i(3) === true
    i(2) === false
    i(4) === false
  }

  test("diff") {


    val d = Diff(s123, s345)

    d(3) === false
    d(1) === true
    d(2) === true
    d(4) === false
    d(5) === false


  }
  test("memory used") {
    showMemory

  }

}
