package pakkio.chesschallenge

// Native-compatible version without parallel collections and complex Scala features
case class NativeCompatibleSolver(m: Int, n: Int, pieces: InitialPieces) {
  type Solutions = Set[Set[PieceAtSlot]]
  
  // Use simple mutable collections instead of concurrent ones
  private val seenCanonical = scala.collection.mutable.Set[String]()
  private var cacheHits = 0
  private var cacheMisses = 0
  
  private val pieceList = flatPieces(pieces)

  lazy val solution: Solutions = {
    val startTime = System.nanoTime()
    val result = placePiecesOptimized(pieceList, m, n)
    val totalTime = (System.nanoTime() - startTime) / 1000000.0
    printStatistics(totalTime)
    result
  }
  
  def count: Int = solution.size

  private def printStatistics(totalTime: Double): Unit = {
    val totalLookups = cacheHits + cacheMisses
    val hitRate = if (totalLookups > 0) (cacheHits.toDouble / totalLookups * 100) else 0.0
    println(f"Native Compatible Statistics:")
    println(f"Total time: ${totalTime}ms")
    println(f"Cache hits: $cacheHits, misses: $cacheMisses (${hitRate}%.1f%% hit rate)")
  }

  private def canonicalForm(disposition: Set[PieceAtSlot]): String = {
    cacheMisses += 1
    val allForms = List(
      disposition,
      disposition.map(rotate90),
      disposition.map(rotate180),
      disposition.map(rotate270),
      disposition.map(reflectX),
      disposition.map(reflectY),
      disposition.map(reflectXThenRotate90),
      disposition.map(reflectYThenRotate90)
    ).map(dispositionToString)
    
    allForms.min
  }

  private def dispositionToString(disposition: Set[PieceAtSlot]): String = {
    val sorted = disposition.toList.sortBy(p => (p.slot.x, p.slot.y, p.piece.toString))
    sorted.map(p => s"${p.piece.mnemonic}${p.slot.x}${p.slot.y}").mkString(",")
  }

  private def rotate90(pieceAtSlot: PieceAtSlot): PieceAtSlot = {
    val Slot(x, y) = pieceAtSlot.slot
    PieceAtSlot(pieceAtSlot.piece, Slot(n - 1 - y, x))
  }
  
  private def rotate180(pieceAtSlot: PieceAtSlot): PieceAtSlot = {
    val Slot(x, y) = pieceAtSlot.slot  
    PieceAtSlot(pieceAtSlot.piece, Slot(m - 1 - x, n - 1 - y))
  }
  
  private def rotate270(pieceAtSlot: PieceAtSlot): PieceAtSlot = {
    val Slot(x, y) = pieceAtSlot.slot
    PieceAtSlot(pieceAtSlot.piece, Slot(y, m - 1 - x))
  }
  
  private def reflectX(pieceAtSlot: PieceAtSlot): PieceAtSlot = {
    val Slot(x, y) = pieceAtSlot.slot
    PieceAtSlot(pieceAtSlot.piece, Slot(m - 1 - x, y))
  }
  
  private def reflectY(pieceAtSlot: PieceAtSlot): PieceAtSlot = {
    val Slot(x, y) = pieceAtSlot.slot
    PieceAtSlot(pieceAtSlot.piece, Slot(x, n - 1 - y))
  }
  
  private def reflectXThenRotate90(pieceAtSlot: PieceAtSlot): PieceAtSlot = {
    rotate90(reflectX(pieceAtSlot))
  }
  
  private def reflectYThenRotate90(pieceAtSlot: PieceAtSlot): PieceAtSlot = {
    rotate90(reflectY(pieceAtSlot))
  }

  private def placePiecesOptimized(pieces: List[Piece], m: Int, n: Int): Solutions = {
    pieces match {
      case List() => Set(Set())
      case piece :: rest =>
        val dispositions = placePiecesOptimized(rest, m, n)
        
        // Sequential processing only (no parallel collections)
        val builder = Set.newBuilder[Set[PieceAtSlot]]
        
        for (disposition <- dispositions) {
          builder ++= getValidPlacements(piece, disposition, m, n)
        }
        
        builder.result()
    }
  }
  
  private def getValidPlacements(piece: Piece, disposition: Set[PieceAtSlot], m: Int, n: Int): Set[Set[PieceAtSlot]] = {
    // Simple board creation without complex collections
    val occupiedSlots = disposition.map(_.slot)
    val allSlots = (for {
      x <- 0 until m
      y <- 0 until n
    } yield Slot(x, y)).toSet
    
    val availableSlots = allSlots -- occupiedSlots
    
    val results = scala.collection.mutable.Set[Set[PieceAtSlot]]()
    
    for (slot <- availableSlots) {
      // Create board for attack calculation
      val tempBoard = Board(m, n, disposition)
      val attackedSlots = piece.getAttackedSlots(tempBoard, slot)
      val attacksExistingPiece = (attackedSlots & occupiedSlots).nonEmpty
      
      if (!attacksExistingPiece) {
        var isAttackedByExisting = false
        for (existingPiece <- disposition if !isAttackedByExisting) {
          val existingAttacks = existingPiece.piece.getAttackedSlots(tempBoard, existingPiece.slot)
          if (existingAttacks.contains(slot)) {
            isAttackedByExisting = true
          }
        }
        
        if (!isAttackedByExisting) {
          val newDisposition = disposition + PieceAtSlot(piece, slot)
          val canonical = canonicalForm(newDisposition)
          
          if (!seenCanonical.contains(canonical)) {
            seenCanonical += canonical
            results += newDisposition
          }
        }
      }
    }
    
    results.toSet
  }
  
  private def flatPieces(pieces: InitialPieces): List[Piece] = {
    val pieceList = for {
      (p, n) <- pieces.list
      _ <- 1 to n
    } yield p
    pieceList.toList
  }
}