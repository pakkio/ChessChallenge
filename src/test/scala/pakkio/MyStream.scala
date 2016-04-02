package pakkio

/**
  * Created by Claudio Pacchiega on 02/04/2016.
  * This software is freely usable by anyone
  */
object MyStream {
  def main(args: Array[String]) {
    def isPrime(x: Int): Boolean = {
      x match {
        case 0 | 1 | 2 => true
        case _ =>
          val l = List(2) ++ (3 to Math.sqrt(x).toInt)
          l.forall(e => x % e != 0)
      }

    }
    val primes = Stream.range(100000, 200000).filter(isPrime)(5)
    val stream = (1 to 100000000).toStream
    println(stream(4))
  }

}
