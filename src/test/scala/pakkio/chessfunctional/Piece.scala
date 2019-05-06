package pakkio.chessfunctional



abstract class Piece(val name: String){
  val pos: P
  def attacks: (P) => Boolean
}

case class King(override val pos: P) extends Piece("K") {
  override def attacks: (P) => Boolean = {
    (p) =>
      //println(s"checking if this King in $optionalPos is attacking $p")
      (math.abs(p.x - pos.x) <= 1) &&
        (math.abs(p.y - pos.y) <= 1)
  }
}

case class Queen(override val pos: P) extends Piece("Q") {
  override def attacks: (P) => Boolean = {
    (p) =>
          // same row or columns
          p.x == pos.x || p.y == pos.y ||
            // same diagonal
            math.abs(p.x - pos.x) == math.abs(p.y - pos.y)
  }
}

case class Rook(override val pos: P) extends Piece("R") {
  override def attacks: (P) => Boolean = {
    (p) =>
          // same row or columns
          p.x == pos.x || p.y == pos.y
  }
}

case class Bishop(override val pos: P) extends Piece("B") {
  override def attacks: (P) => Boolean = {
    (p) =>
          // same diagonal
          math.abs(p.x - pos.x) == math.abs(p.y - pos.y)
  }
}

case class Knight(override val pos: P) extends Piece("N") {
  override def attacks: (P) => Boolean = {
    (p) => {
      //print(p)
      val diffx = math.abs(p.x - pos.x)
      val diffy = math.abs(p.y - pos.y)
      //print(s"diffx: $diffx, diffy: $diffy")
      val ret =
        ((diffx == 1) && (diffy == 2)) ||
          ((diffx == 2) && (diffy == 1))
      //println(s" returning: $ret")
      ret
    }
  }

}

case class P(x:Int, y:Int)