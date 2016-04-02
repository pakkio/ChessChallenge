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

  def index(p: P) = p.x * size + p.y
  case class Rec(name: String, index: Int) {
    override def toString = name + index
  }

  // the way key is built is very important to find duplicates
  // we must be sure we really have the same key for different
  //
  def key = pieces
    .map(piece => Rec(piece.name, index(piece.pos)))
    .sortBy(_.index)
    .mkString


  // creating a Scene with added piece, tentatively put in originally safe
  // slots. This solution avoids mutability
  def insert(f : (P) => Piece): Seq[Scene] = {
    val tentatives: Seq[Piece] = initialSafe map ( p => f(p) )
    val forList = for {
      tentative <- tentatives
    } yield {
      if (pieces.forall(p => !tentative.attacks(p.pos))) {
        val newSafe = initialSafe.filter(p =>
          p != tentative.pos && !tentative.attacks(p)
        )
        val newScene = Scene(size, pieces :+ tentative, newSafe, union(attack, tentative.attacks))
        // avoid introducing duplicates... This is the weakest part of this solution since
        // it is mutable
        if (Duplicates.check(newScene.key)) None
        else
          Some(newScene)
      }
      else None
    }
    val ret = forList.flatten

    ret
  }

}



