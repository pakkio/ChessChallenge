package pakkio.chessfunctional

/**
  * Created by Claudio Pacchiega on 02/04/2016.
  * This software is freely usable by anyone
  */
case class Scene(override val size: Int,
                 override val pieces: Seq[Piece],
                 initialSafe: Seq[P],
                 attack: (P) => Boolean)

  extends WithPieces {

  // concatenates attacks for all the pieces
  def union(s: (P) => Boolean, t: (P) => Boolean): (P) => Boolean =
    (p) => s(p) || t(p)



  // the way key is built is very important to find duplicates
  // we must be sure we really have the same key for different
  //
  def key = {
    def index(p: P) = p.x * size + p.y

    case class Rec(name: String, index: Int) {
      override def toString = name + index
    }
    pieces
      .map(piece => Rec(piece.name, index(piece.pos)))
      .sortBy(_.index)
      .mkString
  }


  // creating a Scene with added piece, tentatively put in originally safe
  // slots. This solution avoids mutability, expressed everything in term of
  // for comprehension so to avoid flat and intermediate Options which are
  // cpu/time expensive
  def insert(f: (P) => Piece): Seq[Scene] = {
    val tentatives: Seq[Piece] = initialSafe map (p => f(p))
    for {
      tentative <- tentatives if pieces.forall(p => !tentative.attacks(p.pos))
      newSafe = initialSafe.filter(p =>
        p != tentative.pos && !tentative.attacks(p)
      )
      newScene = Scene(size, pieces :+ tentative, newSafe, union(attack, tentative.attacks))
      if !Duplicates.check(newScene.key)
    } yield newScene
  }

}



