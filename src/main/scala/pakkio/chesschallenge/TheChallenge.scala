package pakkio.chesschallenge


case class Slot(x:Int,y:Int)
case class Piece(s:Symbol)
case class PieceAtSlot(p:Piece, s:Slot)
case class Pieces(list:Map[Piece,Int])

case class Board(m:Int,n:Int, content:List[PieceAtSlot]=Nil) {
  def attacks(s: Slot) = {
    val attacked = for {
      p <- content
      slot <- attackedSlots(p)
    } yield( slot )
    attacked.contains(s)
  }
  
  def attackedSlots(p: PieceAtSlot) : List[Slot] = {
    val piece=p.p
    val s=p.s

    piece match {
      case Piece('king) =>
        // king moves on every 8 positions around
        val l=for {
          x <- s.x - 1 to s.x + 1
          y <- s.y - 1 to s.y + 1
          slot = Slot(x, y)
          if (canMoveHere(slot))
        } yield (slot)

        l.toList


      case Piece('rook) =>
        // initial implementation naif not contemplating "shadowed" pieces
        val l=for {
          x <- 0 until m
          y <- 0 until n
          slot = Slot(x,y)
          if(x==s.x && y==s.y && canMoveHere(slot))

        } yield(slot)
        l.toList


        // not yet implemented
      case _ => Nil
        ???

    }
  }


  // this returns either a List of 1 element of Nothing
  def getPieceAtPosition(pFind:Slot):List[Piece] = {
    content.filter( _.s == pFind ).map(_.p)
  }
  def canMoveHere(s:Slot):Boolean = {
    (s.x >= 0 && s.x < m && s.y>=0 && s.y<n && getPieceAtPosition(s)==Nil)
  }
  // create a new Board with added piece
  def addAPiece(p:PieceAtSlot) = {
    Board(m,n,p::content)
  }

  def availableSlots() = for {
    i<- 0 until m
    j<- 0 until n
    pos = Slot(i,j)
    p = getPieceAtPosition(pos)
    if(p==Nil)
  } yield(pos)
}





