package pakkio.chesschallenge

case class Solution(m:Int, n:Int, pieces:Pieces) {

  type Solutions = List[Board]

  // obtain a list of all the pieces to insert from the map
  // flattening down the count to proper repetition of the piece
  private val pieceList = flatPieces(pieces)
  val solution = placePieces(pieceList,m,n)
  println("Computed solutions")

  def count = solution.size

  private def flatPieces(pieces:Pieces) = {
    val pieceList = for {
      (p,n) <- pieces.list
      counter <- 1 to n

    } yield p
    pieceList.toList
  }

  // recursive function to place all residual pieces
  private def placePieces(l:List[Piece],m:Int,n:Int): Solutions = {

    l match {
      case List() => List(Board(m,n))
      case piece :: rest =>
        println("Processing piece: "+piece)
        for {
          b <- placePieces(rest,m,n)
          // find an available slot
          slot <- b.availableSlots()

          pAtSlot = PieceAtSlot(piece,slot)
          // only in a safe position of the board
          if b.wouldBeOKIfAddingThisPiece(pAtSlot)



        } yield b.addAPiece(pAtSlot)
    }
  }
  def printSolutions =

  for {
    board <- solution

  } yield board.printBoard("")

}
