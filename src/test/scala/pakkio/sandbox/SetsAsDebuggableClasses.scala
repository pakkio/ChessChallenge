package pakkio.sandbox

object SetsAsDebuggableClasses {

  val label = "Testing sets as debuggable classes"

  abstract class Set(val name:String) {
    def apply(x:Int): Boolean
    override def toString = name
  }
  /*abstract class Set(name1:String) {
    def apply(x:Int): Boolean
    def name: String = ""
    override def toString = ""
  }*/
  case class S(value: Int)
    extends Set(""+value) {
    override def apply(x: Int) = x == value
  }

  case class Union(s:Set, t:Set)
    extends Set("Union(" + s.name + "," + t.name + ")") {
    override def apply(x: Int) = s(x) || t(x)
  }

  case class Intersect(s:Set, t:Set)
    extends Set("Intersect(" + s.name + "," + t.name + ")") {
    override def apply(x: Int) = s(x) && t(x)
  }

  case class Diff(s:Set, t:Set)
    extends Set("Diff(" + s.name + "," + t.name + ")") {
    override def apply(x: Int) = s(x) && !t(x)
  }

  val s1 = S(1)
  val s2 = S(2)
  val s3 = S(3)
  val s4 = S(4)
  val s5 = S(5)

  val s123 = Union(s1,Union(s2,s3))
  val s345 = Union(s3,Union(s4,s5))

}
