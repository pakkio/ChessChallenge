package pakkio.chessfunctional

import org.scalatest.FunSuite
import pakkio.sandbox.{CheckMemory, Testable}

class TestBoard extends FunSuite with CheckMemory with Testable {

  val label = "TestBoard"

  def getFreeSlots(m:Int): Seq[P] = {

    // an initial completely safe board m x n
    for {
      i <- 0 until m
      j <- 0 until m
    } yield P(i, j)
  }

  test("printing board") {
    class TestPieces extends WithPieces {
      override val size: Int = 2
      override val pieces: Seq[Piece] = Seq(new Rook(P(0, 0)))
    }
    val printed = (new TestPieces).toString

    assert(printed === " - -\n R -\n ")
  }

  test("testing final exercise") {
    val noAttacks : (P) => Boolean = (p) => false
    val K=7
    val emptyBoard=List(Scene(K, List(), getFreeSlots(K), noAttacks))
    // 7x7 King -> 2 , Queen -> 2, Bishop -> 2, Knight -> 1

    val finalList = for {
      q0 <- emptyBoard
      q1 <- q0.insert(Queen)
      q2 <- q1.insert(Queen)
      q3 <- q2.insert(Bishop)
      q4 <- q3.insert(Bishop)
      q5 <- q4.insert(King)
      q6 <- q5.insert(King)
      q7 <- q6.insert(Knight)

    } yield q7

//    val finalList = emptyBoard
//      .flatMap(_.insert(Queen)).par
//      .flatMap(_.insert(Queen)).par
//      .flatMap(_.insert(Bishop)).par
//      .flatMap(_.insert(Bishop)).par
//      .flatMap(_.insert(King)).par
//      .flatMap(_.insert(King)).par
//      .flatMap(_.insert(Knight)).par


    //finalList.map(println)
    println(finalList.head)
    println(finalList.last)

    /*val printed = finalList.map(_.toString)
    val unique = printed.distinct
    val erroneous = printed.diff(unique)*/

    assert(finalList.size === 3063828)

    println(s"Found ${finalList.size} combinations expected 3063828")


  }
  test("show statistics") {
    showMemory
  }
}
