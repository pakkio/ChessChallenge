package pakkio.chesschallenge

import java.util.concurrent.ConcurrentHashMap
import scala.collection.mutable

// Hybrid approach: MCTS-style guidance with systematic exhaustive exploration
case class GuidedExhaustiveSolver(m: Int, n: Int, pieces: InitialPieces) {
  type Solutions = Set[Set[PieceAtSlot]]
  
  // Use symmetry elimination like the current best solver
  private val seenCanonical = new ConcurrentHashMap[String, Boolean]()
  private val hashCache = scala.collection.mutable.HashMap[Set[PieceAtSlot], String]()
  private var cacheHits = 0
  private var cacheMisses = 0
  
  // Smart piece ordering from MCTS: hardest pieces first
  private val pieceList = smartPieceOrdering(flatPieces(pieces))
  
  lazy val solution: Solutions = {
    val startTime = System.nanoTime()
    println("Starting Guided Exhaustive Solver...")
    println(s"Piece order: ${pieceList.map(_.mnemonic).mkString(" -> ")}")
    
    val result = placePiecesGuided(pieceList, m, n)
    
    val totalTime = (System.nanoTime() - startTime) / 1000000.0
    printStatistics(totalTime)
    result
  }
  
  def count: Int = solution.size
  
  private def printStatistics(totalTime: Double): Unit = {
    val totalLookups = cacheHits + cacheMisses
    val hitRate = if (totalLookups > 0) (cacheHits.toDouble / totalLookups * 100) else 0.0
    println(f"Guided Exhaustive Statistics:")
    println(f"Total time: ${totalTime}ms")
    println(f"Cache hits: $cacheHits, misses: $cacheMisses (${hitRate}%.1f%% hit rate)")
  }
  
  // Smart piece ordering: most constrained pieces first
  private def smartPieceOrdering(pieces: List[Piece]): List[Piece] = {
    pieces.sortBy { piece =>
      piece match {
        case Queen => 0   // Queens are most constrained (attack everywhere)
        case King => 1    // Kings next (attack adjacent squares)  
        case Bishop => 2  // Bishops (attack diagonals)
        case Knight => 3  // Knights least constrained (L-shaped moves)
        case _ => 4
      }
    }
  }
  
  private def placePiecesGuided(pieces: List[Piece], m: Int, n: Int): Solutions = {
    pieces match {
      case List() => Set(Set())
      case piece :: rest =>
        val dispositions = placePiecesGuided(rest, m, n)
        
        // Use guided placement for each existing disposition
        val results = if (dispositions.size > 500) {
          // Parallel processing for large sets
          val parallelDispositions = dispositions.par
          parallelDispositions.flatMap { disposition =>
            getGuidedPlacements(piece, disposition, m, n)
          }.seq.toSet
        } else {
          val builder = Set.newBuilder[Set[PieceAtSlot]]
          builder.sizeHint(dispositions.size * 4) // Conservative estimate
          
          for (disposition <- dispositions) {
            builder ++= getGuidedPlacements(piece, disposition, m, n)
          }
          
          builder.result()
        }
        
        results
    }
  }
  
  // Guided placement: systematic exploration with smart ordering
  private def getGuidedPlacements(piece: Piece, disposition: Set[PieceAtSlot], m: Int, n: Int): Set[Set[PieceAtSlot]] = {
    val baseBoard = Board(m, n, disposition)
    val availableSlots = baseBoard.availableSlots.toList
    val occupiedSlots = disposition.map(_.slot)
    
    // Smart slot ordering: prefer positions that are more likely to lead to solutions
    val orderedSlots = availableSlots.sortBy(slot => calculateSlotPriority(slot, piece, disposition, baseBoard))
    
    val results = mutable.Set[Set[PieceAtSlot]]()
    results.sizeHint(orderedSlots.size / 3)
    
    for (slot <- orderedSlots) {
      // Early pruning: check if this placement could possibly work
      if (isPromissingPlacement(piece, slot, disposition, baseBoard)) {
        val attackedSlots = piece.getAttackedSlots(baseBoard, slot)
        val attacksExistingPiece = (attackedSlots & occupiedSlots).nonEmpty
        
        if (!attacksExistingPiece) {
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
            
            // Use symmetry elimination to avoid duplicates
            val canonical = canonicalForm(newDisposition)
            if (seenCanonical.putIfAbsent(canonical, true) == null) {
              results += newDisposition
            }
          }
        }
      }
    }
    
    results.toSet
  }
  
  // Calculate priority score for slot (lower = higher priority)
  private def calculateSlotPriority(slot: Slot, piece: Piece, disposition: Set[PieceAtSlot], board: Board): Double = {
    var priority = 0.0
    
    // Prefer center positions (slight bias)
    val centerX = m / 2.0
    val centerY = n / 2.0
    val distanceFromCenter = math.sqrt(math.pow(slot.x - centerX, 2) + math.pow(slot.y - centerY, 2))
    priority += distanceFromCenter * 0.1 // Small center bias
    
    // Prefer positions that don't over-constrain remaining pieces
    val attackedSlots = piece.getAttackedSlots(board, slot)
    val remainingSlots = board.availableSlots.size - 1
    if (remainingSlots > 0) {
      val constraintRatio = attackedSlots.size.toDouble / remainingSlots
      priority += constraintRatio * 2.0 // Penalty for high constraint
    }
    
    // Prefer corners and edges for certain pieces
    val isCorner = (slot.x == 0 || slot.x == m-1) && (slot.y == 0 || slot.y == n-1)
    val isEdge = slot.x == 0 || slot.x == m-1 || slot.y == 0 || slot.y == n-1
    
    piece match {
      case King => 
        if (isCorner) priority -= 0.5 // Kings like corners
      case Knight => 
        if (!isEdge) priority -= 0.3 // Knights prefer interior
      case _ =>
    }
    
    priority
  }
  
  // Quick check if a placement looks promising
  private def isPromissingPlacement(piece: Piece, slot: Slot, disposition: Set[PieceAtSlot], board: Board): Boolean = {
    // Basic bounds check
    if (!board.isValidPosition(slot)) return false
    
    // Check if slot is already occupied
    if (disposition.exists(_.slot == slot)) return false
    
    // For now, assume all valid slots are promising
    // Could add more sophisticated heuristics here
    true
  }
  
  // Symmetry elimination (same as CarefulOptimizedSolver)
  private def canonicalForm(disposition: Set[PieceAtSlot]): String = {
    hashCache.get(disposition) match {
      case Some(cached) =>
        cacheHits += 1
        cached
      case None =>
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
        
        val canonical = allForms.min
        
        if (hashCache.size < 10000) {
          hashCache(disposition) = canonical
        }
        
        canonical
    }
  }
  
  private def dispositionToString(disposition: Set[PieceAtSlot]): String = {
    val sorted = disposition.toArray.sortBy(p => (p.slot.x, p.slot.y, p.piece.toString))
    val sb = new StringBuilder(sorted.length * 4)
    
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
  
  // Symmetry transformations (same as CarefulOptimizedSolver)
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
  
  private def flatPieces(pieces: InitialPieces): List[Piece] = {
    val pieceList = for {
      (p, n) <- pieces.list
      _ <- 1 to n
    } yield p
    pieceList.toList
  }
}