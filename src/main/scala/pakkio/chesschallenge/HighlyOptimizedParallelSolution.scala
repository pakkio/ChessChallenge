package pakkio.chesschallenge

// Highly optimized parallel solution with advanced techniques
case class HighlyOptimizedParallelSolution(m: Int, n: Int, pieces: InitialPieces) {
  type Solutions = Set[Set[PieceAtSlot]]

  // Obtain a list of all the pieces to insert from the map
  private val pieceList = flatPieces(pieces)

  // This computes all the solutions using highly optimized parallel processing
  lazy val solution: Solutions = placePiecesHighlyOptimized(pieceList, m, n)

  def count: Int = solution.size

  private def flatPieces(pieces: InitialPieces): List[Piece] = {
    val pieceList = for {
      (p, n) <- pieces.list
      _ <- 1 to n
    } yield p
    pieceList.toList
  }

  // Highly optimized parallel recursive function with advanced techniques
  private def placePiecesHighlyOptimized(l: List[Piece], m: Int, n: Int): Solutions = {
    l match {
      case List() => Set(Set())
      case piece :: rest =>
        val dispositions = placePiecesHighlyOptimized(rest, m, n)
        
        // Use adaptive processing based on workload size
        if (dispositions.size < 1000) {
          // For smaller workloads, use sequential processing to avoid parallelization overhead
          sequentialProcessing(piece, m, n, dispositions)
        } else {
          // For larger workloads, use parallel processing with optimized parameters
          optimizedParallelProcessing(piece, m, n, dispositions)
        }
    }
  }
  
  // Sequential processing for smaller workloads
  private def sequentialProcessing(piece: Piece, m: Int, n: Int, dispositions: Set[Set[PieceAtSlot]]): Solutions = {
    val builder = Set.newBuilder[Set[PieceAtSlot]]
    
    for (disposition <- dispositions) {
      val b = Board(m, n, disposition)
      val availableSlots = b.availableSlots
      
      for (slot <- availableSlots) {
        val newb = b.addAPiece(PieceAtSlot(piece, slot))
        if (newb.isSafe) {
          builder += newb.content
        }
      }
    }
    
    builder.result()
  }
  
  // Optimized parallel processing with better memory management
  private def optimizedParallelProcessing(piece: Piece, m: Int, n: Int, dispositions: Set[Set[PieceAtSlot]]): Solutions = {
    // Convert to parallel collection
    val parallelDispositions = dispositions.par
    
    // Use tasksplitter for better workload distribution
    val results = parallelDispositions.flatMap { disposition =>
      val b = Board(m, n, disposition)
      val availableSlots = b.availableSlots
      
      // Pre-size the collection for better performance
      val intermediate = scala.collection.mutable.ArrayBuffer[Set[PieceAtSlot]]()
      
      // Use while loop for better performance
      val slotsList = availableSlots.toList
      var i = 0
      while (i < slotsList.length) {
        val slot = slotsList(i)
        val newb = b.addAPiece(PieceAtSlot(piece, slot))
        if (newb.isSafe) {
          intermediate += newb.content
        }
        i += 1
      }
      
      intermediate
    }
    
    results.seq.toSet
  }

  // Debug utility to print boards
  def printSolutions =
    for {
      disposition <- solution
    } yield Board(m, n, disposition).printBoard("")
}