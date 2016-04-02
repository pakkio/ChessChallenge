package pakkio.pakkio.chessfunctional

import scala.collection.immutable.Nil

/**
  * Created by Claudio Pacchiega on 02/04/2016.
  * This software is freely usable by anyone
  */
trait  WithPieces {
  val size: Int
  val pieces: Seq[Piece]

  def mkElement(p: Option[Piece]) = {
    p match {
      case None => "-"
      case Some(p1) => p1.name
    }
  }

  def getPieceAtPosition(c: Int, r: Int): Option[Piece] = {
    val filtered = pieces.filter(p => p.getP.x == c && p.getP.y == r)
    val ret = filtered match {
      case Nil => None
      case p :: _ => Some(p)

    }
    ret
  }

  def mkLine(r: Int, max: Int): String = {
    val l = for {
      c <- 0 to max
      p = getPieceAtPosition(c, r)
    } yield mkElement(p)
    val ret = l.toList.mkString(" ") + "\n "
    ret

  }

  def printPieces = {
    val l = for {
      r <- (size - 1) to 0 by -1
    } yield mkLine(r, size - 1)
    val ret = l.toList.mkString(" ", "", "")
    ret
  }

  override def toString = {
    printPieces
  }


}
