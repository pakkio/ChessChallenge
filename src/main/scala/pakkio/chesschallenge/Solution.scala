package pakkio.chesschallenge

case class Solution(m:Int, n:Int, pieces:InitialPieces) {

  type Solutions = Set[Set[PieceAtSlot]]

  // obtain a list of all the pieces to insert from the map
  // flattening down the count to proper repetition of the piece
  private val pieceList = flatPieces(pieces)
  val solution = placePieces(pieceList,m,n)

  def count = solution.size

  private def flatPieces(pieces:InitialPieces) = {
    val pieceList = for {
      (p,n) <- pieces.list
      counter <- 1 to n

    } yield p
    pieceList.toList
  }

  // recursive function to place all residual pieces
  private def placePieces(l:List[Piece],m:Int,n:Int): Solutions = {

    l match {
      case List() => Set(Set())
      case piece :: rest =>
        for {
          disposition <- placePieces(rest,m,n)
          b=Board(m,n,disposition)
          // find an available slot
          slot <- b.availableSlots

          newb = b.addAPiece(PieceAtSlot(piece,slot))
          // only in a safe position of the board
          if newb.isSafe

        } yield
        newb.content
    }
  }
  def printSolutions =

  for {
    disposition <- solution

  } yield Board(m,n,disposition).printBoard("")

}
