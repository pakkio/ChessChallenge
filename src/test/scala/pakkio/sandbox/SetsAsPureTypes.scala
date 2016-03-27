package pakkio.sandbox

object SetsAsPureTypes {
  type Set = Int => Boolean

  def SingletonSet(elem: Int): Set =
    (x: Int) => x==elem

  def Union(s: Set, t: Set): Set =
    (x) => s(x) || t(x)

  def Intersect(s:Set, t:Set): Set =
    (x) => s(x) && t(x)

  def Diff(s:Set, t:Set): Set =
    (x) => s(x) && !t(x)

  val s1 = SingletonSet(1)
  val s2 = SingletonSet(2)
  val s3 = SingletonSet(3)
  val s4 = SingletonSet(4)
  val s5 = SingletonSet(5)

  val s123 = Union(s1,Union(s2,s3))
  val s345 = Union(s3,Union(s4,s5))


}


