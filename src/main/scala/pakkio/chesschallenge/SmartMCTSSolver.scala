package pakkio.chesschallenge

import scala.util.Random
import scala.collection.mutable

// Smarter MCTS solver with better heuristics and pruning
case class SmartMCTSSolver(m: Int, n: Int, pieces: InitialPieces, 
                          maxIterations: Int = 5000,
                          explorationConstant: Double = 1.414) {
  
  type BoardState = Set[PieceAtSlot]
  type Solutions = Set[BoardState]
  
  private val random = new Random()
  private val pieceList = flatPieces(pieces)
  
  // Smart piece ordering: harder pieces first
  private val orderedPieces = pieceList.sortBy {
    case Queen => 0    // Queens are most constrained, place first
    case King => 1     // Kings next
    case Bishop => 2   // Bishops  
    case Knight => 3   // Knights are least constrained
    case _ => 4
  }
  
  lazy val solution: Solutions = {
    val solutions = mutable.Set[BoardState]()
    val startTime = System.nanoTime()
    
    // Use a more systematic approach: guided search with some randomness
    var attempts = 0
    
    while (attempts < maxIterations && solutions.size < 50) {
      val solution = smartPlacement()
      solution.foreach(solutions += _)
      
      attempts += 1
      
      if (attempts % 500 == 0) {
        val elapsed = (System.nanoTime() - startTime) / 1000000.0
        println(s"Smart MCTS: $attempts attempts, ${solutions.size} solutions found, ${elapsed}ms elapsed")
      }
    }
    
    val totalTime = (System.nanoTime() - startTime) / 1000000.0
    println(s"Smart MCTS completed: $attempts attempts, ${solutions.size} unique solutions, ${totalTime}ms")
    
    solutions.toSet
  }
  
  def count: Int = solution.size
  
  // Smart placement using guided random search
  private def smartPlacement(): Option[BoardState] = {
    var currentState: BoardState = Set.empty
    
    for (piece <- orderedPieces) {
      val board = Board(m, n, currentState)
      val validPlacements = getValidPlacements(piece, currentState, board)
      
      if (validPlacements.isEmpty) {
        return None // Dead end, no valid placement
      }
      
      // Smart selection: bias towards center positions and less attacked positions
      val scoredPlacements = validPlacements.map { slot =>
        val score = calculatePlacementScore(piece, slot, currentState, board)
        (slot, score)
      }
      
      // Use weighted random selection instead of pure random
      val selectedSlot = weightedRandomSelection(scoredPlacements)
      currentState = currentState + PieceAtSlot(piece, selectedSlot)
    }
    
    Some(currentState)
  }
  
  private def getValidPlacements(piece: Piece, currentState: BoardState, board: Board): List[Slot] = {
    val availableSlots = board.availableSlots
    val occupiedSlots = currentState.map(_.slot)
    
    val validSlots = mutable.ListBuffer[Slot]()
    
    for (slot <- availableSlots) {
      val attackedSlots = piece.getAttackedSlots(board, slot)
      val attacksExistingPiece = (attackedSlots & occupiedSlots).nonEmpty
      
      if (!attacksExistingPiece) {
        val isAttackedByExisting = currentState.exists { existingPiece =>
          val existingAttacks = existingPiece.piece.getAttackedSlots(board, existingPiece.slot)
          existingAttacks.contains(slot)
        }
        
        if (!isAttackedByExisting) {
          validSlots += slot
        }
      }
    }
    
    validSlots.toList
  }
  
  // Calculate a heuristic score for piece placement
  private def calculatePlacementScore(piece: Piece, slot: Slot, currentState: BoardState, board: Board): Double = {
    var score = 0.0
    
    // Bias towards center positions (but not too strongly)
    val centerX = m / 2.0
    val centerY = n / 2.0
    val distanceFromCenter = math.sqrt(math.pow(slot.x - centerX, 2) + math.pow(slot.y - centerY, 2))
    val maxDistance = math.sqrt(math.pow(centerX, 2) + math.pow(centerY, 2))
    val centerScore = 1.0 - (distanceFromCenter / maxDistance)
    score += centerScore * 0.3 // 30% weight for center preference
    
    // Prefer positions that don't overly constrain future placements
    val attackedSlots = piece.getAttackedSlots(board, slot)
    val remainingSlots = board.availableSlots.size - 1 // -1 for this piece
    val constraintPenalty = attackedSlots.size.toDouble / remainingSlots
    score -= constraintPenalty * 0.4 // 40% penalty for high constraint
    
    // Add some randomness to avoid getting stuck in local optima
    score += random.nextDouble() * 0.3 // 30% randomness
    
    score
  }
  
  // Weighted random selection based on scores
  private def weightedRandomSelection(scoredPlacements: List[(Slot, Double)]): Slot = {
    if (scoredPlacements.length == 1) return scoredPlacements.head._1
    
    // Normalize scores to be positive
    val minScore = scoredPlacements.map(_._2).min
    val adjustedPlacements = scoredPlacements.map { case (slot, score) => 
      (slot, score - minScore + 0.1) // Add small constant to avoid zero weights
    }
    
    val totalWeight = adjustedPlacements.map(_._2).sum
    val randomValue = random.nextDouble() * totalWeight
    
    var cumulativeWeight = 0.0
    for ((slot, weight) <- adjustedPlacements) {
      cumulativeWeight += weight
      if (randomValue <= cumulativeWeight) {
        return slot
      }
    }
    
    // Fallback to last element
    adjustedPlacements.last._1
  }
  
  private def flatPieces(pieces: InitialPieces): List[Piece] = {
    val pieceList = for {
      (p, n) <- pieces.list
      _ <- 1 to n
    } yield p
    pieceList.toList
  }
}