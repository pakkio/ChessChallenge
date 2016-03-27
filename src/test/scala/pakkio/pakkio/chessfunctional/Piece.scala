package pakkio.pakkio.chessfunctional

// case class allows usage of .copy to generate
// new pieces in immutable ways
case class Piece(
                  optionalPos: Option[P] = None,
                  name: String = "Piece",
                  attacks: (P) => Boolean =
                    (p) => false) {

  lazy val getP = {
    if (optionalPos.isEmpty) throw new AttackException
    optionalPos.get
  }

}

class King(override val optionalPos: Option[P] = None) extends Piece {
  override val attacks: (P) => Boolean = {
    (p) =>
      println(s"checking if this King in $optionalPos is attacking $p")
      (math.abs(p.x - getP.x) <= 1) &&
        (math.abs(p.y - getP.y) <= 1) &&
        p != getP
  }

  override val name: String = "King"
}

class Queen(override val optionalPos: Option[P] = None) extends Piece {
  override val attacks: (P) => Boolean = {
    (p) =>
      p != getP &&
        (
          // same row or columns
          p.x == getP.x || p.y == getP.y ||
            // same diagonal
            math.abs(p.x - getP.x) == math.abs(p.y - getP.y)
          )
  }

  override val name: String = "Queen"
}

class Rook(override val optionalPos: Option[P] = None) extends Piece {
  override val attacks: (P) => Boolean = {
    (p) =>
      p != getP &&
        (
          // same row or columns
          p.x == getP.x || p.y == getP.y
          )
  }

  override val name: String = "Rook"
}

class Bishop(override val optionalPos: Option[P] = None) extends Piece {
  override val attacks: (P) => Boolean = {
    (p) =>
      p != getP &&
        (
          // same diagonal
          math.abs(p.x - getP.x) == math.abs(p.y - getP.y)
          )
  }

  override val name: String = "Bishop"
}

class Knight(override val optionalPos: Option[P] = None) extends Piece {
  override val attacks: (P) => Boolean = {
    (p) => {
      //print(p)
      val diffx = math.abs(p.x - getP.x)
      val diffy = math.abs(p.y - getP.y)
      //print(s"diffx: $diffx, diffy: $diffy")
      val ret = p != getP && (
        ((diffx == 1) && (diffy == 2)) ||
          ((diffx == 2) && (diffy == 1)))
      //println(s" returning: $ret")
      ret
    }
  }

  override val name: String = "Knight"
}


