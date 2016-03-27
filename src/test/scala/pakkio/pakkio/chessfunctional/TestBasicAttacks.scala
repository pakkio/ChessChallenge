package pakkio.pakkio.chessfunctional

import org.scalatest.FunSuite

class TestBasicAttacks extends FunSuite {

  test("Testing a King attack") {
    val king = new King(Some(P(1, 1)))
    val list: List[(Int, Int)] = createSlots
    val results: List[Rec] = positionsUnderAttack(king, list)
    val correctResults: List[Int] =
      List(
        1, 1, 1, 0,
        1, 0, 1, 0,
        1, 1, 1, 0,
        0, 0, 0, 0,
        0, 0)

    compare("King",results,correctResults)

  }
  test("Testing a Queen attack") {
    val queen = new Queen(Some(P(1,1)))
    // reuse king's list
    val list: List[(Int, Int)] = createSlots
    val results: List[Rec] = positionsUnderAttack(queen, list)
    val correctResults: List[Int] =
      List(
        1, 1, 1, 0,
        1, 0, 1, 1,
        1, 1, 1, 0,
        0, 1, 0, 1,
        0, 1)
    compare("Queen",results,correctResults)
  }
  test("Testing a Rook attack") {
    val rook = new Rook(Some(P(1,1)))
    // reuse king's list
    val list: List[(Int, Int)] = createSlots
    val results: List[Rec] = positionsUnderAttack(rook, list)
    val correctResults: List[Int] =
      List(
        0, 1, 0, 0,
        1, 0, 1, 1,
        0, 1, 0, 0,
        0, 1, 0, 0,
        0, 0)
    compare("Rook",results,correctResults)
  }
  test("Testing a Bishop attack") {
    val queen = new Bishop(Some(P(1,1)))
    // reuse king's list
    val list: List[(Int, Int)] = createSlots
    val results: List[Rec] = positionsUnderAttack(queen, list)
    val correctResults: List[Int] =
      List(
        1, 0, 1, 0,
        0, 0, 0, 0,
        1, 0, 1, 0,
        0, 0, 0, 1,
        0, 1)
    compare("Bishop",results,correctResults)
  }
  test("Testing a Knight attack") {
    val queen = new Knight(Some(P(1,1)))
    // reuse king's list
    val list: List[(Int, Int)] = createSlots
    val results: List[Rec] = positionsUnderAttack(queen, list)
    val correctResults: List[Int] =
      List(
        0, 0, 0, 1,
        0, 0, 0, 0,
        0, 0, 0, 1,
        1, 0, 1, 0,
        0, 0)
    compare("Knight",results,correctResults)
  }

  def positionsUnderAttack(piece: Piece, list: List[(Int, Int)]): List[Rec] = {
    val results = list.map(
      { case p@(x: Int, y: Int) => Rec(p,
        piece.attacks(
          P(x, y)
        )
      )
      }
    )
    results
  }


  def createSlots: List[(Int, Int)] = {
    val list = (
      (for {
        i <- 0 to 3
        j <- 0 to 3
      } yield (i, j)) :+(10, 0) :+ (9,9)
      ).toList
    list
  }

  def compare(title:String,results: List[Rec], correctResults: List[Int]) = {
    val comparing = results.zip(correctResults)

    val forall = comparing.forall(
      { case (p: Rec, flag: Int) => {
        print(title,"checking ",p.p, p.b, flag)
        val ret = if (p.b) (flag == 1) else (flag == 0)
        if(!ret)
          println(s" ret: $ret")
        ret
      }
      }
    )
    println("forall is ",forall)
    assert(forall===true)
  }

}

case class Rec(p: (Int, Int), b: Boolean)
