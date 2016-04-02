package pakkio.pakkio.chessfunctional

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
      override val pieces: Seq[Piece] = Seq(new Rook(Some(P(0, 0))))
    }

    (new TestPieces).toString === " -- --\n Ro --\n"


  }

  test("test1") {
    val noAttacks : (P,P) => Boolean = (me,p) => false
    val K=7
    val list=List(Scene(K, List(), getFreeSlots(K), noAttacks))
    // 7x7 King -> 2 , Queen -> 2, Bishop -> 2, Knight -> 1

    val finalList = list.par
      .flatMap(s => s.insert(new Queen)).par
      .flatMap(s => s.insert(new Queen)).par
      .flatMap(s => s.insert(new Bishop)).par
      .flatMap(s => s.insert(new Bishop)).par
      .flatMap(s => s.insert(new King)).par
      .flatMap(s => s.insert(new King)).par
      .flatMap(s => s.insert(new Knight)).par


    //finalList.map(println)
    println(finalList.head)
    println(finalList.last)

    finalList.size === 3063828

    println(s"Found ${finalList.size} combinations")


  }
  test("show statistics") {
    showMemory
  }
}
