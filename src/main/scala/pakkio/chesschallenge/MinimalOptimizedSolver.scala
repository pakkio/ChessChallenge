package pakkio.chesschallenge

import java.util.concurrent.ConcurrentHashMap

// Minimal optimization: ONLY reverse piece ordering (Kings first)
case class MinimalOptimizedSolver(m: Int, n: Int, pieces: InitialPieces) {
  type Solutions = Set[Set[PieceAtSlot]]

  // Symmetry elimination  
  private val seenCanonical = new ConcurrentHashMap[String, Boolean]()
  
  // Same conservative ML threshold as Simple ML
  private val PREDICTION_THRESHOLD = 0.10
  
  // Statistics
  private var totalPredictions = 0
  private var prunedMoves = 0

  // Same conservative weights as Simple ML
  private val THREATENS_EXISTING_WEIGHT = -4.7
  private val THREATENED_BY_EXISTING_WEIGHT = -4.1  
  private val NEARBY_PIECES_WEIGHT = -0.4
  private val CENTER_BIAS_WEIGHT = 0.2
  private val ATTACK_EFFICIENCY_WEIGHT = 0.1

  // ONLY CHANGE: Reverse piece ordering (least restrictive first)
  private val pieceList = reverseOrderPieces(flatPieces(pieces))

  lazy val solution: Solutions = {
    val result = placePiecesOptimized(pieceList, m, n)
    printStatistics()
    result
  }
  
  def count: Int = solution.size

  private def printStatistics(): Unit = {
    if (totalPredictions > 0) {
      val pruningRate = (prunedMoves.toDouble / totalPredictions * 100)
      println(f"Minimal Opt Statistics: $prunedMoves/$totalPredictions moves pruned (${pruningRate}%.1f%%)")
    }
  }

  private def flatPieces(pieces: InitialPieces): List[Piece] = {
    val pieceList = for {
      (p, n) <- pieces.list
      _ <- 1 to n
    } yield p
    pieceList.toList
  }
  
  // OPTIMIZATION: Place least restrictive pieces first (reverse of previous attempt)
  private def reverseOrderPieces(pieces: List[Piece]): List[Piece] = {
    // Place least restrictive pieces first to make quick early decisions
    val restrictiveness = Map(
      King -> 1,     // Least restrictive (only adjacent) -> Place FIRST
      Knight -> 2,   // Low restrictive (L-shape) -> Place SECOND  
      Bishop -> 3,   // Medium restrictive (diagonal) -> Place THIRD
      Queen -> 4     // Most restrictive (long-range) -> Place LAST
    )
    
    pieces.sortBy(p => restrictiveness.getOrElse(p, 0))
  }

  // Same ML prediction as Simple ML (no changes)
  private def predictPlacementSuccess(piece: Piece, slot: Slot, disposition: Set[PieceAtSlot], 
                                    board: Board): Double = {
    totalPredictions += 1
    
    val features = extractMLFeatures(piece, slot, disposition, board)
    
    val score = 
      features.threatensExisting * THREATENS_EXISTING_WEIGHT +
      features.threatenedByExisting * THREATENED_BY_EXISTING_WEIGHT +
      features.nearbyPieces * NEARBY_PIECES_WEIGHT +
      features.centerBias * CENTER_BIAS_WEIGHT +
      features.attackEfficiency * ATTACK_EFFICIENCY_WEIGHT
    
    val probability = 1.0 / (1.0 + math.exp(-score))
    probability
  }

  case class MLFeatures(
    threatensExisting: Double,
    threatenedByExisting: Double, 
    nearbyPieces: Double,
    centerBias: Double,
    attackEfficiency: Double
  )

  // Same feature extraction as Simple ML
  private def extractMLFeatures(piece: Piece, slot: Slot, disposition: Set[PieceAtSlot], 
                               board: Board): MLFeatures = {
    val attackedSlots = piece.getAttackedSlots(board, slot)
    val occupiedSlots = disposition.map(_.slot)
    
    val threatenedByExisting = if (disposition.exists { existingPiece =>
      val existingAttacks = existingPiece.piece.getAttackedSlots(board, existingPiece.slot)
      existingAttacks.contains(slot)
    }) 1.0 else 0.0
    
    val threatensExisting = if ((attackedSlots & occupiedSlots).nonEmpty) 1.0 else 0.0
    
    val nearbyPieces = disposition.count { pieceAtSlot =>
      val dx = math.abs(pieceAtSlot.slot.x - slot.x)
      val dy = math.abs(pieceAtSlot.slot.y - slot.y)
      dx <= 2 && dy <= 2 && !(dx == 0 && dy == 0)
    }.toDouble
    
    val centerX = (m - 1) / 2.0
    val centerY = (n - 1) / 2.0
    val centerDistance = math.sqrt(math.pow(slot.x - centerX, 2) + math.pow(slot.y - centerY, 2))
    val centerBias = 1.0 / (1.0 + centerDistance)
    
    val attackEfficiency = attackedSlots.size.toDouble / math.max(1.0, (m * n) - disposition.size)
    
    MLFeatures(
      threatensExisting,
      threatenedByExisting,
      nearbyPieces,
      centerBias,
      attackEfficiency
    )
  }

  // Same symmetry elimination as Simple ML
  private def canonicalForm(disposition: Set[PieceAtSlot]): String = {
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

  private def dispositionToString(disposition: Set[PieceAtSlot]): String = {
    disposition.toList
      .sortBy(p => (p.slot.x, p.slot.y, p.piece.toString))
      .map(p => s"${p.piece.mnemonic}${p.slot.x}${p.slot.y}")
      .mkString(",")
  }

  // Same recursive algorithm as Simple ML
  private def placePiecesOptimized(l: List[Piece], m: Int, n: Int): Solutions = {
    l match {
      case List() => Set(Set())
      case piece :: rest =>
        val dispositions = placePiecesOptimized(rest, m, n)
        
        if (dispositions.size > 1000) {
          val parallelDispositions = dispositions.par
          val results = parallelDispositions.flatMap { disposition =>
            getValidPlacementsMinimal(piece, disposition, m, n)
          }
          results.seq.toSet
        } else {
          val builder = Set.newBuilder[Set[PieceAtSlot]]
          
          for (disposition <- dispositions) {
            builder ++= getValidPlacementsMinimal(piece, disposition, m, n)
          }
          
          builder.result()
        }
    }
  }
  
  // Same placement logic as Simple ML (no changes except name)
  private def getValidPlacementsMinimal(piece: Piece, disposition: Set[PieceAtSlot], m: Int, n: Int): Set[Set[PieceAtSlot]] = {
    val baseBoard = Board(m, n, disposition)
    val availableSlots = baseBoard.availableSlots
    val occupiedSlots = disposition.map(_.slot)
    
    val results = scala.collection.mutable.Set[Set[PieceAtSlot]]()
    
    for (slot <- availableSlots) {
      val newPieceAtSlot = PieceAtSlot(piece, slot)
      
      val attackedSlots = piece.getAttackedSlots(baseBoard, slot)
      val attacksExistingPiece = (attackedSlots & occupiedSlots).nonEmpty
      
      if (!attacksExistingPiece) {
        val isAttackedByExisting = disposition.exists { existingPiece =>
          val existingAttacks = existingPiece.piece.getAttackedSlots(baseBoard, existingPiece.slot)
          existingAttacks.contains(slot)
        }
        
        if (!isAttackedByExisting) {
          val successProbability = predictPlacementSuccess(piece, slot, disposition, baseBoard)
          
          if (successProbability >= PREDICTION_THRESHOLD) {
            val newDisposition = disposition + newPieceAtSlot
            
            val canonical = canonicalForm(newDisposition)
            if (seenCanonical.putIfAbsent(canonical, true) == null) {
              results += newDisposition
            }
          } else {
            prunedMoves += 1
          }
        }
      }
    }
    
    results.toSet
  }
}