package pakkio.chesschallenge

import java.util.concurrent.ConcurrentHashMap

// Careful optimized solver: Speed improvements with minimal accuracy loss
case class CarefulOptimizedSolver(m: Int, n: Int, pieces: InitialPieces) {
  type Solutions = Set[Set[PieceAtSlot]]

  // HYBRID APPROACH: Use both hash and string for better accuracy
  private val seenCanonical = new ConcurrentHashMap[String, Boolean]()
  private val hashCache = scala.collection.mutable.HashMap[Set[PieceAtSlot], String]()
  
  // Statistics
  private var cacheHits = 0
  private var cacheMisses = 0
  
  private val pieceList = flatPieces(pieces)

  lazy val solution: Solutions = {
    val result = placePiecesOptimized(pieceList, m, n)
    printStatistics()
    result
  }
  
  def count: Int = solution.size

  private def printStatistics(): Unit = {
    val totalLookups = cacheHits + cacheMisses
    val hitRate = if (totalLookups > 0) (cacheHits.toDouble / totalLookups * 100) else 0.0
    println(f"Careful Opt Statistics:")
    println(f"Cache hits: $cacheHits, misses: $cacheMisses (${hitRate}%.1f%% hit rate)")
  }

  private def flatPieces(pieces: InitialPieces): List[Piece] = {
    val pieceList = for {
      (p, n) <- pieces.list
      _ <- 1 to n
    } yield p
    pieceList.toList
  }

  // OPTIMIZATION 1: Cached string canonicalization (best of both worlds)
  private def canonicalForm(disposition: Set[PieceAtSlot]): String = {
    hashCache.get(disposition) match {
      case Some(cached) =>
        cacheHits += 1
        cached
      case None =>
        cacheMisses += 1
        // Use original string-based approach but cache the result
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
        
        val canonical = allForms.min
        
        // Cache the result for future lookups
        if (hashCache.size < 10000) { // Prevent unbounded growth
          hashCache(disposition) = canonical
        }
        
        canonical
    }
  }

  // OPTIMIZATION 2: Faster string generation (StringBuilder instead of concatenation)
  private def dispositionToString(disposition: Set[PieceAtSlot]): String = {
    val sorted = disposition.toArray.sortBy(p => (p.slot.x, p.slot.y, p.piece.toString))
    val sb = new StringBuilder(sorted.length * 4) // Pre-sized for efficiency
    
    var i = 0
    while (i < sorted.length) {
      val p = sorted(i)
      sb.append(p.piece.mnemonic)
      sb.append(p.slot.x)
      sb.append(p.slot.y)
      if (i < sorted.length - 1) {
        sb.append(',')
      }
      i += 1
    }
    
    sb.toString
  }

  // Same transformation functions (optimized with while loops)
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

  private def placePiecesOptimized(l: List[Piece], m: Int, n: Int): Solutions = {
    l match {
      case List() => Set(Set())
      case piece :: rest =>
        val dispositions = placePiecesOptimized(rest, m, n)
        
        // OPTIMIZATION 3: Slightly more aggressive parallelization
        if (dispositions.size > 750) { // Sweet spot between 500 and 1000
          val parallelDispositions = dispositions.par
          val results = parallelDispositions.flatMap { disposition =>
            getValidPlacementsCareful(piece, disposition, m, n)
          }
          results.seq.toSet
        } else {
          val builder = Set.newBuilder[Set[PieceAtSlot]]
          builder.sizeHint(dispositions.size * 8) // Reasonable hint
          
          for (disposition <- dispositions) {
            builder ++= getValidPlacementsCareful(piece, disposition, m, n)
          }
          
          builder.result()
        }
    }
  }
  
  // Careful placement finder with minimal optimizations
  private def getValidPlacementsCareful(piece: Piece, disposition: Set[PieceAtSlot], m: Int, n: Int): Set[Set[PieceAtSlot]] = {
    val baseBoard = Board(m, n, disposition)
    val availableSlots = baseBoard.availableSlots
    val occupiedSlots = disposition.map(_.slot)
    
    val results = scala.collection.mutable.Set[Set[PieceAtSlot]]()
    results.sizeHint(availableSlots.size / 3) // Conservative estimate
    
    for (slot <- availableSlots) {
      val attackedSlots = piece.getAttackedSlots(baseBoard, slot)
      val attacksExistingPiece = (attackedSlots & occupiedSlots).nonEmpty
      
      if (!attacksExistingPiece) {
        // OPTIMIZATION 4: Early exit on first attack found
        var isAttackedByExisting = false
        val iterator = disposition.iterator
        while (iterator.hasNext && !isAttackedByExisting) {
          val existingPiece = iterator.next()
          val existingAttacks = existingPiece.piece.getAttackedSlots(baseBoard, existingPiece.slot)
          if (existingAttacks.contains(slot)) {
            isAttackedByExisting = true
          }
        }
        
        if (!isAttackedByExisting) {
          val newDisposition = disposition + PieceAtSlot(piece, slot)
          
          // Use cached canonicalization (preserves correctness)
          val canonical = canonicalForm(newDisposition)
          if (seenCanonical.putIfAbsent(canonical, true) == null) {
            results += newDisposition
          }
        }
      }
    }
    
    results.toSet
  }
}