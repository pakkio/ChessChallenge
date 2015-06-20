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
    } yield slot
    //println(attacked)
    attacked.contains(s)
  }

  // find out which slots are attacked by a piece in a specified slot
  def attackedSlots(p: PieceAtSlot): List[Slot] = {
    // to be able to TakeWhileInclusive otherwise we will need to have scalaz
    import ImplicitIterator._
    val piece = p.piece
    val s = p.slot

    val list = for {

      // gets one of the possible paths
      p <- piece.getPaths(this, s)
    } yield p.toIterator.takeWhileInclusive(isFree).toList

    val ret = list.flatten
    ret
  }

  // this returns an option if a piece is there
  def getPieceAtPosition(pFind: Slot): Option[Piece] = {
    content.filter(_.slot == pFind).map(_.piece).headOption
  }

  // check if a slot is free (inverse of the previous function)
  def isFree(pFind: Slot): Boolean = {
    getPieceAtPosition(pFind).isEmpty
  }
  // canMoveHere just check if position in inside the board
  def isValidPosition(s: Slot) =
    s.x >= 0 && s.x < m && s.y >= 0 && s.y < n

  // create a new Board with added piece
  def addAPiece(p: PieceAtSlot) = {
    //println("adding piece " + p.piece + " at slot " + p.slot)
    Board(m, n, p :: content)
  }

  // find slots where we can possibly insert pieces
  def availableSlots() = for {
    i <- 0 until m
    j <- 0 until n
    pos = Slot(i, j)
    p = getPieceAtPosition(pos)
    if p.isEmpty
  } yield pos

  // verify if a tobe added piece at slot would be safe for all the other pieces
  def wouldBeOKIfAddingThisPiece(pAtSlot:PieceAtSlot) = {
    val slot=pAtSlot.slot
    if(attacks(slot))
      false // not a safe board
    else {
      // and providing this piece is not attacking other pieces
      val alreadyPresentPieces = content.map(_.slot)

      // if intersection is empty then we are safe
      val ret=attackedSlots(pAtSlot).intersect(alreadyPresentPieces) == List()
      if(ret) println("Adding solution") // Debug
      ret
    }

  }

  def mkElement(p: Option[Piece]) = {
    p match {
      case None    => "--"
      case Some(p1) => p1.shortName
    }
  }

  def mkLine(r: Int) = {
    val l = for {
      c <- 0 until m
      p = getPieceAtPosition(Slot(c, r))
    } yield mkElement(p)
    val ret=l.toList.mkString(" ") + "\n "
    ret

  }

  def printBoard(s: String) =
    {
      val l = for {
        r <- n - 1 to 0 by -1
      } yield mkLine(r)
      val ret=l.toList.mkString(" ", "", "")
      println(ret)
    }

}