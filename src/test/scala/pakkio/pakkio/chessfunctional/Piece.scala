package pakkio.pakkio.chessfunctional

// case class allows usage of .copy to generate
// new pieces in immutable ways
case class Piece(
                  optionalPos: Option[P] = None,
                  name: String = "Piece",
                  attacks: (P, P) => Boolean =
                  (me, p) => false) {

  def getP = {
    if (optionalPos.isEmpty)
      throw new AttackException
    optionalPos.get
  }

}

class King(override val optionalPos: Option[P] = None) extends Piece {
  override val attacks: (P, P) => Boolean = {
    (me, p) =>
      //println(s"checking if this King in $optionalPos is attacking $p")
      // p != me &&
      (math.abs(p.x - me.x) <= 1) &&
        (math.abs(p.y - me.y) <= 1)
  }

  override val name: String = "King"
}

class Queen(override val optionalPos: Option[P] = None) extends Piece {
  override val attacks: (P, P) => Boolean = {
    (me, p) =>
      //p != me &&
      // same row or columns
      p.x == me.x || p.y == me.y ||
        // same diagonal
        math.abs(p.x - me.x) == math.abs(p.y - me.y)
  }

  override val name: String = "Queen"
}

class Rook(override val optionalPos: Option[P] = None) extends Piece {
  override val attacks: (P, P) => Boolean = {
    (me, p) =>
      //p != me &&
      // same row or columns
      p.x == me.x || p.y == me.y
  }

  override val name: String = "Rook"
}

class Bishop(override val optionalPos: Option[P] = None) extends Piece {
  override val attacks: (P, P) => Boolean = {
    (me, p) =>
      //p != me &&
      // same diagonal
      math.abs(p.x - me.x) == math.abs(p.y - me.y)
  }

  override val name: String = "Bishop"
}

class Knight(override val optionalPos: Option[P] = None) extends Piece {
  override val attacks: (P, P) => Boolean = {
    (me, p) => {
      //print(p)
      val diffx = math.abs(p.x - me.x)
      val diffy = math.abs(p.y - me.y)
      //print(s"diffx: $diffx, diffy: $diffy")
      //val ret = p != me && (
      val ret = ((diffx == 1) && (diffy == 2)) ||
        ((diffx == 2) && (diffy == 1))
      //println(s" returning: $ret")
      ret
    }
  }

  override val name: String = "Knight"
}


