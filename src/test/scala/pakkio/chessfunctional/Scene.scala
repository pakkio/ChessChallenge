package pakkio.chessfunctional

/**
  * Created by Claudio Pacchiega on 02/04/2016.
  * This software is freely usable by anyone
  */
case class Scene (override val size: Int,
                  override val pieces:Seq[Piece],
                  initialSafe:Seq[P],
                  attack: (P) => Boolean)

  extends WithPieces {

  def union(s: (P) => Boolean, t: (P) => Boolean): (P) => Boolean =
    (p) => s(p) || t(p)

  // creating a Scene with added piece, tentatively put in originally safe
  // slots. This solution avoids mutability
  def insert(f : (P) => Piece): Seq[Scene] = {
    val tentatives: Seq[Piece] = initialSafe map ( p => f(p) )
    val forList = for {
      tentative <- tentatives
    } yield {
      if (pieces.forall(p => !tentative.attacks(p.pos))) {
        val newSafe = initialSafe.filter(
          p => {
            val me = tentative.pos
            p != me && !tentative.attacks(p)
          }
        )
        val newScene = Scene(size, pieces :+ tentative, newSafe, union(attack, tentative.attacks))
        // avoid introducing duplicates... This is the weakest part of this solution since
        // it is mutable
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



