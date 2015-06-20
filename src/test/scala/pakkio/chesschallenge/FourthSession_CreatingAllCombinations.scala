package pakkio.chesschallenge

import org.scalatest.FunSuite

import scala.util.{ Failure, Try }
class FourthSession_CreatingAllCombinations extends FunSuite {
  

  private def checkSolutionsAreExactly(pieces:Pieces, m:Int, n:Int, expected:Int, debug:Boolean=false) = {
    val result = Solution(m,n,pieces)
    if(debug) result.printSolutions
    assert(result.count == expected)
  }

  test("2 Queens") {
    checkSolutionsAreExactly(
        Pieces(Map(Queen -> 2) ),
        3,2,
        4)
  }
  test("1 Kings and a Knight") {
    checkSolutionsAreExactly(
      Pieces(Map(King -> 1, Knight -> 1)),
      3,3,
      16)
  }

  test("the real challenge") {
    checkSolutionsAreExactly(
      Pieces(Map(King -> 1 , Queen -> 1, Bishop -> 1, Knight -> 1)),
      7,7,
      5,
      debug=true
    )
  }


}
