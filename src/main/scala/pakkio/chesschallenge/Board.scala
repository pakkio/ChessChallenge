package pakkio.chesschallenge


// a slot simply is defined by its horizontal and vertical coordinates
// x going right, y going up
case class Slot(x: Int, y: Int)

// have a chess piece at a specified position in the board
case class PieceAtSlot(piece: Piece, slot: Slot)

// initial configuration is modeled as a map of Piece and number of elements
case class InitialPieces(list: Map[Piece, Int])

object Board {
  var count=0 // this is used just to monitor
}
// Board is defined by its 
case class Board(m: Int, n: Int, content: Set[PieceAtSlot] = Set())
  extends ShowBoard {

  // Shows on the console an incremental hint on how many boards we are scanning
  Board.count += 1
  if(Board.count % 100000 == 0) println(s"Instantiating ${Board.count}")

  // precomputes all the unsafeslots avoiding duplicates
  val unSafeSlots:Set[Slot] = {
    val l = for {
      p <- content
    } yield p.piece.getAttackedSlots(this, p.slot)
    val ret=l.flatten.toSet
    ret
  }

  // understand which pieces are being attacked in current configuration
  val unsafePieces:Set[PieceAtSlot] = {
    val l = for {
      p <- content
      attackedList = p.piece.getAttackedSlots(this,p.slot)
      attackedPieces = {
        val ret=content.filter(el => attackedList.contains(el.slot) && p.slot!=el.slot )
        ret
      }
    } yield attackedPieces
    val ret=l.flatten.toSet
    ret
  }

  // is this a "safe" i.e. a board where all pieces are in peace one with all the others?
  def isSafe = unsafePieces == Set()


  // find slots where we can possibly insert pieces which is not under attack
  val availableSlots = {

    val allSlots= for {
      i <- 0 until m
      j <- 0 until n
      pos = Slot(i, j)
    } yield pos

    // available slots are the full content without the unsafe slots and without pieces already there
    allSlots.toSet
      .diff(unSafeSlots)
      .diff(content.map(_.slot).toSet)
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
    Board(m, n, content + p)
  }

}


trait ShowBoard {
  self: Board =>

  def mkElement(p: Option[Piece]) = {
    p match {
      case None    => "-"
      case Some(p1) => p1.mnemonic
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

  def printBoard(s: String="") =
  {
    val l = for {
      r <- n - 1 to 0 by -1
    } yield mkLine(r)
    val ret=l.toList.mkString(" ", "", "")
    println(ret)
  }
}