package pakkio.chesschallenge

import org.scalatest.FunSuite

import scala.util.{ Failure, Try }
class T03_CreatingAllCombinationsAndAssessingSolutions extends FunSuite {
  

  private def checkSolutionsAreExactly(pieces:InitialPieces, m:Int, n:Int, expected:Int, debug:Boolean=false) = {
    Board.count=0
    val result = Solution(m,n,pieces)
    if(debug) result.printSolutions
    else {
      // just print a solution
      Board(m,n,result.solution.head).printBoard()
    }
    assert(result.count == expected)
  }

  test("2 Queens") {
    checkSolutionsAreExactly(
        InitialPieces(Map(Queen -> 2) ),
        3,2,
        2,true)
  }
  test("1 Kings and a Knight") {

    checkSolutionsAreExactly(
      InitialPieces(Map(King -> 1, Knight -> 1)),
      3,3,
      16, true)
  }

  test("the real challenge with 5x5 is taking around 2.5 seconds") {
    checkSolutionsAreExactly(
      InitialPieces(Map(King -> 2 , Queen -> 2, Bishop -> 2, Knight -> 1)),
      5,5,
      8,
      debug=true
    )
  }
  test("the real challenge with 6x6 is taking around 42 seconds") {
    checkSolutionsAreExactly(
      InitialPieces(Map(King -> 2 , Queen -> 2, Bishop -> 2, Knight -> 1)),
      6,6,
      23752,
      debug=false
    )
  }

  test("the real challenge with 7x7 is taking around 27 minutes giving virtual machine -Xmx8G") {
    checkSolutionsAreExactly(
      InitialPieces(Map(King -> 2 , Queen -> 2, Bishop -> 2, Knight -> 1)),
      7,7,
      3063828,
      debug=false
    )
  }




}
