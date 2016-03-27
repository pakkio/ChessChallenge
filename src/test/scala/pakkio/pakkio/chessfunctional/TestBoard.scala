package pakkio.pakkio.chessfunctional

import org.scalatest.FunSuite

import scala.collection.immutable.Nil

case class PiecePos(piece:Piece,pos:P)

case class Scene(
                  pieces:List[Piece],
                  initialSafe:List[P],
                  attack: (P) => Boolean) {

  def union(s: (P) => Boolean, t: (P) => Boolean): (P) => Boolean =
    (p) => s(p) || t(p)

  // creating a Scene with original pieces, original attack function
  // and a new Piece
  def insert(piece:Piece):List[Scene] = {
    //piece.copy()
    /*if(pieces.forall( p=> !piece.attacks(p.optionalPos)))
      aPiece <- pieces
    } yield {
      if (piece.attacks(aPiece.x)) None
      else {
        val newSafe = initialSafe.filter(
          p => p != tentativePos && !piece.attacks(p))
        Scene(pieces :+ piece, newSafe, union(attack, piece.attacks))
      }
    }*/
    Nil
  }

}

class TestBoard extends FunSuite {

  val N = 3
  // an initial completely safe board 3x3
  val initialSafe: List[P] = (for {
    i <- 0 to N
    j <- 0 to N
  } yield P(i,j)).toList

  test("test1") {
    val noAttacks : (P) => Boolean = (p) => false
    val s=Scene(List(), initialSafe, noAttacks)
    val list=s.insert(new Queen)

    println(list)

  }
}
