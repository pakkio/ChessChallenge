package pakkio.pakkio.chessfunctional

// case class allows usage of .copy to generate
// new pieces in immutable ways
case class Piece( name: String,
                  optionalPos: Option[P] = None,
                  attacks: (P,P) => Boolean =
                    (me,p) => false) {

  def getP = {
    if (optionalPos.isEmpty)
      throw new AttackException
    optionalPos.get
  }




}

class King(override val optionalPos: Option[P] = None) extends Piece("K") {
  override val attacks: (P,P) => Boolean = {
    (me,p) =>
      //println(s"checking if this King in $optionalPos is attacking $p")
      (math.abs(p.x - me.x) <= 1) &&
        (math.abs(p.y - me.y) <= 1)
  }


}

class Queen(override val optionalPos: Option[P] = None) extends Piece("Q") {
  override val attacks: (P,P) => Boolean = {
    (me,p) =>
          // same row or columns
          p.x == me.x || p.y == me.y ||
            // same diagonal
            math.abs(p.x - me.x) == math.abs(p.y - me.y)
  }

}

class Rook(override val optionalPos: Option[P] = None) extends Piece("R") {
  override val attacks: (P,P) => Boolean = {
    (me,p) =>
          // same row or columns
          p.x == me.x || p.y == me.y
  }

}

class Bishop(override val optionalPos: Option[P] = None) extends Piece("B") {
  override val attacks: (P,P) => Boolean = {
    (me,p) =>
          // same diagonal
          math.abs(p.x - me.x) == math.abs(p.y - me.y)
  }

}

class Knight(override val optionalPos: Option[P] = None) extends Piece("N") {
  override val attacks: (P,P) => Boolean = {
    (me,p) => {
      //print(p)
      val diffx = math.abs(p.x - me.x)
      val diffy = math.abs(p.y - me.y)
      //print(s"diffx: $diffx, diffy: $diffy")
      val ret =
        ((diffx == 1) && (diffy == 2)) ||
          ((diffx == 2) && (diffy == 1))
      //println(s" returning: $ret")
      ret
    }
  }

}


