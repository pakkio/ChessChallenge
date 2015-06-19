package pakkio.chesschallenge

// a slot simply is defined by its horizontal and vertical coordinates
// x going right, y going up
case class Slot(x: Int, y: Int)

// have a chess piece at a specified position in the board
case class PieceAtSlot(piece: Piece, slot: Slot)

// initial configuration is modeled as a map of Piece and number of elements
case class Pieces(list: Map[Piece, Int])

// Board is defined by its 
case class Board(m: Int, n: Int, content: List[PieceAtSlot] = Nil) {

  // find out if a slot is attacked by some other piece
  def attacks(s: Slot) = {
    val attacked = for {
      p <- content
      slot <- attackedSlots(p)
    } yield (slot)
    //println(attacked)
    attacked.contains(s)
  }

  // find out which slots are attacked by a piece in a specified slot
  def attackedSlots(p: PieceAtSlot): List[Slot] = {

    

    val piece = p.piece
    val s = p.slot

    val list=for {

      // gets one of the possible paths
      p <- piece.getPaths(this, s)
    } yield (p.takeWhile(isFree))
     
      
    val ret=list.flatten
    ret
  }

  // this returns an option if a piece is there
  def getPieceAtPosition(pFind: Slot): Option[Piece] = {
    content.filter(_.slot == pFind).map(_.piece).headOption
  }
  
  // check if a slot is free (inverse of the previous function)
  def isFree(pFind:Slot):Boolean = {
    getPieceAtPosition(pFind) == None
  }
  // canMoveHere just check if position in inside the board
  def isValidPosition(s: Slot) =
    s.x >= 0 && s.x < m && s.y >= 0 && s.y < n

  // create a new Board with added piece
  def addAPiece(p: PieceAtSlot) = {
    Board(m, n, p :: content)
  }

  // find slots where we can possibly insert pieces
  def availableSlots() = for {
    i <- 0 until m
    j <- 0 until n
    pos = Slot(i, j)
    p = getPieceAtPosition(pos)
    if (p == None)
  } yield (pos)
}





