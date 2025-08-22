package pakkio.chesschallenge

// Parallel version of the solution for better performance on multi-core systems
case class ParallelSolution(m: Int, n: Int, pieces: InitialPieces) {
  type Solutions = Set[Set[PieceAtSlot]]

  // Obtain a list of all the pieces to insert from the map
  private val pieceList = flatPieces(pieces)

  // This computes all the solutions using parallel processing
  lazy val solution: Solutions = placePiecesParallel(pieceList, m, n)

  def count: Int = solution.size

  private def flatPieces(pieces: InitialPieces): List[Piece] = {
    val pieceList = for {
      (p, n) <- pieces.list
      _ <- 1 to n
    } yield p
    pieceList.toList
  }

  // Parallel recursive function
  private def placePiecesParallel(l: List[Piece], m: Int, n: Int): Solutions = {
    l match {
      case List() => Set(Set())
      case piece :: rest =>
        val dispositions = placePiecesParallel(rest, m, n)
        
        // Process dispositions in parallel
        val parallelDispositions = dispositions.par
        
        val results = for {
          disposition <- parallelDispositions
          b = Board(m, n, disposition)
          // Find an available slot
          slot <- b.availableSlots
          newb = b.addAPiece(PieceAtSlot(piece, slot))
          // Only in a safe position of the board
          if newb.isSafe
        } yield newb.content
        
        results.seq.toSet
    }
  }

  // Debug utility to print boards
  def printSolutions =
    for {
      disposition <- solution
    } yield Board(m, n, disposition).printBoard("")
}