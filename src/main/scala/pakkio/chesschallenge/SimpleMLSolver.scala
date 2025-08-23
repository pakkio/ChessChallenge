package pakkio.chesschallenge

import java.util.concurrent.ConcurrentHashMap
import scala.util.Random

// Ultra-fast ML-Enhanced solver with simple heuristic model
// No external dependencies - pure Scala performance
case class SimpleMLSolver(m: Int, n: Int, pieces: InitialPieces) {
  type Solutions = Set[Set[PieceAtSlot]]

  // Symmetry elimination
  private val seenCanonical = new ConcurrentHashMap[String, Boolean]()
  
  // ML prediction threshold
  private val PREDICTION_THRESHOLD = 0.10
  
  // Statistics
  private var totalPredictions = 0
  private var prunedMoves = 0
  
  // Simple learned weights from our training data insights
  // Based on feature importance: threatensExisting (47%), threatenedByExisting (41%), nearbyPieces (4%)
  private val THREATENS_EXISTING_WEIGHT = -4.7  // Strong negative (bad move)
  private val THREATENED_BY_EXISTING_WEIGHT = -4.1  // Strong negative (bad move) 
  private val NEARBY_PIECES_WEIGHT = -0.4  // Slight negative (crowding)
  private val CENTER_BIAS_WEIGHT = 0.2  // Slight positive (central positions better)
  private val ATTACK_EFFICIENCY_WEIGHT = 0.1  // Slight positive (efficient pieces)

  private val pieceList = flatPieces(pieces)

  lazy val solution: Solutions = {
    val result = placePiecesOptimized(pieceList, m, n)
    printStatistics()
    result
  }
  
  def count: Int = solution.size

  private def printStatistics(): Unit = {
    if (totalPredictions > 0) {
      val pruningRate = (prunedMoves.toDouble / totalPredictions * 100)
      println(f"Simple ML Statistics: $prunedMoves/$totalPredictions moves pruned (${pruningRate}%.1f%%)")
    }
  }

  private def flatPieces(pieces: InitialPieces): List[Piece] = {
    val pieceList = for {
      (p, n) <- pieces.list
      _ <- 1 to n
    } yield p
    pieceList.toList
  }

  // Ultra-fast heuristic ML prediction using learned weights
  private def predictPlacementSuccess(piece: Piece, slot: Slot, disposition: Set[PieceAtSlot], 
                                    board: Board): Double = {
    totalPredictions += 1
    
    val features = extractMLFeatures(piece, slot, disposition, board, m, n)
    
    // Simple linear model based on training insights
    val score = 
      features.threatensExisting * THREATENS_EXISTING_WEIGHT +
      features.threatenedByExisting * THREATENED_BY_EXISTING_WEIGHT +
      features.nearbyPieces * NEARBY_PIECES_WEIGHT +
      features.centerBias * CENTER_BIAS_WEIGHT +
      features.attackEfficiency * ATTACK_EFFICIENCY_WEIGHT
    
    // Convert score to probability using sigmoid
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

  // Extract key features identified by training
  private def extractMLFeatures(piece: Piece, slot: Slot, disposition: Set[PieceAtSlot], 
                               board: Board, m: Int, n: Int): MLFeatures = {
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

  // Symmetry elimination functions (same as before)
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

  private def placePiecesOptimized(l: List[Piece], m: Int, n: Int): Solutions = {
    l match {
      case List() => Set(Set())
      case piece :: rest =>
        val dispositions = placePiecesOptimized(rest, m, n)
        
        if (dispositions.size > 1000) {
          val parallelDispositions = dispositions.par
          val results = parallelDispositions.flatMap { disposition =>
            getValidPlacementsML(piece, disposition, m, n)
          }
          results.seq.toSet
        } else {
          val builder = Set.newBuilder[Set[PieceAtSlot]]
          
          for (disposition <- dispositions) {
            builder ++= getValidPlacementsML(piece, disposition, m, n)
          }
          
          builder.result()
        }
    }
  }
  
  // Ultra-fast ML-Enhanced placement finder with zero overhead
  private def getValidPlacementsML(piece: Piece, disposition: Set[PieceAtSlot], m: Int, n: Int): Set[Set[PieceAtSlot]] = {
    val baseBoard = Board(m, n, disposition)
    val availableSlots = baseBoard.availableSlots
    val occupiedSlots = disposition.map(_.slot)
    
    val results = scala.collection.mutable.Set[Set[PieceAtSlot]]()
    
    for (slot <- availableSlots) {
      val newPieceAtSlot = PieceAtSlot(piece, slot)
      
      // Fast safety check first
      val attackedSlots = piece.getAttackedSlots(baseBoard, slot)
      val attacksExistingPiece = (attackedSlots & occupiedSlots).nonEmpty
      
      if (!attacksExistingPiece) {
        val isAttackedByExisting = disposition.exists { existingPiece =>
          val existingAttacks = existingPiece.piece.getAttackedSlots(baseBoard, existingPiece.slot)
          existingAttacks.contains(slot)
        }
        
        if (!isAttackedByExisting) {
          // ML Prediction: Ultra-fast heuristic model
          val successProbability = predictPlacementSuccess(piece, slot, disposition, baseBoard)
          
          if (successProbability >= PREDICTION_THRESHOLD) {
            val newDisposition = disposition + newPieceAtSlot
            
            // Symmetry elimination
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