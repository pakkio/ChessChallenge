package pakkio.pakkio.chessfunctional

import java.util

object Duplicates {

  val _1G: Double = math.pow(10, 9)
  val _1M: Double = math.pow(10, 6)
  val _1K: Double = math.pow(10, 3)

  def fmt(x: Double) = f"$x%.1f"

  def f(x: Long): String = {
    if (x >= _1G) return fmt(x / _1G) + " G"
    if (x >= _1M) return fmt(x / _1M) + " M"
    if (x >= _1K) return fmt(x / _1K) + " K"
    x + "B"
  }

  var counter: Int = 0
  var counterDuplicates: Int = 0
  val map = new util.HashSet[String]

  def check(s: Scene) = {
    val key = s.toString
    if (map.contains(key)) {
      // println("duplicated entry")
      counterDuplicates += 1
      if (counterDuplicates % 100000 == 0) {
        println(s"Duplicates ${f(counterDuplicates)}")
      }
      true
    } else {
      this.synchronized {}
      map.add(key)
      counter += 1
      if (counter % 100000 == 0) {
        println(s"Adding ${f(counter)}")
      }
      false
    }
  }



}

