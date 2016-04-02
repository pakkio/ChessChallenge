package pakkio.pakkio.chessfunctional

import scala.collection.immutable.Nil

/**
  * Created by Claudio Pacchiega on 02/04/2016.
  * This software is freely usable by anyone
  */
case class Scene (override val size: Int,
                  override val pieces:Seq[Piece],
                  initialSafe:Seq[P],
                  attack: (P,P) => Boolean)

  extends WithPieces {

  def union(s: (P, P) => Boolean, t: (P, P) => Boolean): (P, P) => Boolean =
    (me, p) => s(me, p) || t(me, p)

  // creating a Scene with original pieces, original attack function
  // and a new Piece
  def insert(piece: Piece): Seq[Scene] = {
    val forList = for {
      p <- initialSafe
      tentative = piece.copy(piece.name,Some(p))

    } yield {
      if (pieces.forall(p => !tentative.attacks(tentative.getP, p.getP))) {
        val newSafe = initialSafe.filter(
          p => {
            val me = tentative.getP
            p != me && !tentative.attacks(me, p)
          }
        )
        val newScene = Scene(size, pieces :+ tentative, newSafe, union(attack, tentative.attacks))
        // avoid introducing duplicates
        if (Duplicates.check(newScene)) None
        else
          Some(newScene)
      }
      else None
    }
    val ret = forList.flatten

    ret
  }

}



