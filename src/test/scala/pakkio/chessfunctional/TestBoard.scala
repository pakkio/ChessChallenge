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

  // the idea here is to have an initial board with all free slots and no attacks being held
  // then at each iteration we add a piece in all freeslots, i.e. slots which are free and not under attack
  // at each iteration we spawn a new board for each distinct situation, restricting the "free" slots
  // and augmenting the function specifying which coordinates are under attack.
  // this is a nice functional implementation noAttack is augmented with each further piece placed
  // this makes the algorithm fully parallelizable and using completely immutable data
  // and it is really fast.

  test("testing final exercise") {
    val noAttacks : (P => Boolean) = (p => false)
    val K=7
    val emptyBoard=List(Scene(K, List(), getFreeSlots(K), noAttacks))
    // 7x7 King -> 2 , Queen -> 2, Bishop -> 2, Knight -> 1

    val solutionBoard = for {
      q0 <- emptyBoard
      q1 <- q0.insert(Queen)
      q2 <- q1.insert(Queen)
      q3 <- q2.insert(Bishop)
      q4 <- q3.insert(Bishop)
      q5 <- q4.insert(King)
      q6 <- q5.insert(King)
      q7 <- q6.insert(Knight)

    } yield q7

//    val solutionBoard = emptyBoard
//      .flatMap(_.insert(Queen)).par
//      .flatMap(_.insert(Queen)).par
//      .flatMap(_.insert(Bishop)).par
//      .flatMap(_.insert(Bishop)).par
//      .flatMap(_.insert(King)).par
//      .flatMap(_.insert(King)).par
//      .flatMap(_.insert(Knight)).par


    //solutionBoard.map(println)
    println(solutionBoard.head)
    println(solutionBoard.last)

    /*val printed = solutionBoard.map(_.toString)
    val unique = printed.distinct
    val erroneous = printed.diff(unique)*/

    assert(solutionBoard.size === 3063828)

    println(s"Found ${solutionBoard.size} combinations expected 3063828")


  }
  test("show statistics") {
    showMemory
  }
}
