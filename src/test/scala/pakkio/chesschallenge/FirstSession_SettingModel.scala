package pakkio.chesschallenge

import java.util.NoSuchElementException

import org.scalatest.FunSuite

import scala.util.{Failure, Try}

// BottomUp approach to the problem: be sure we have a way to model our "game"
class FirstSession_SettingModel extends FunSuite {

  // first test ensures that that we have a way to model a Board
  test("Assuming we have a way to build up a board MxN") {
    val b = Board(3, 2)
    b.m === 3
    b.n === 2
  }

  // second test ensures that we have a way to model input of our problem
  test("Have a way to define a listing of chess pieces") {
    val aRook = Rook 
      val aKing = King 
      val aQueen = Queen
      val aKnight = Knight
    val pieces = {
      
      Pieces(Map(
          aRook -> 3, 
          aKing -> 1,
          aQueen -> 2))
    }
    pieces.list(aRook) === 3
    pieces.list(aKing) === 1
    pieces.list(aQueen) === 2
    Try(pieces.list(aKnight)).isFailure === true
  }
}

