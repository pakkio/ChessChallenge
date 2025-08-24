package pakkio.chesschallenge

import scala.util.Random
import scala.collection.mutable

// Pure MCTS solver without neural networks
case class MCTSSolver(m: Int, n: Int, pieces: InitialPieces, 
                     maxIterations: Int = 10000, 
                     explorationConstant: Double = 1.414) {
  
  type BoardState = Set[PieceAtSlot]
  type Solutions = Set[BoardState]
  
  private val random = new Random()
  private val pieceList = flatPieces(pieces)
  
  // MCTS Node representation
  class MCTSNode(
    val state: BoardState,
    val remainingPieces: List[Piece],
    val parent: Option[MCTSNode] = None
  ) {
    var visits: Int = 0
    var wins: Double = 0.0
    var children: mutable.Map[PieceAtSlot, MCTSNode] = mutable.Map.empty
    var isFullyExpanded: Boolean = false
    var possibleMoves: List[PieceAtSlot] = List.empty
    
    // UCB1 score for node selection
    def ucb1Score: Double = {
      if (visits == 0) Double.PositiveInfinity
      else {
        val exploitation = wins / visits
        val exploration = explorationConstant * math.sqrt(math.log(parent.map(_.visits.toDouble).getOrElse(1.0)) / visits)
        exploitation + exploration
      }
    }
    
    // Check if this is a terminal node (win/loss)
    def isTerminal: Boolean = remainingPieces.isEmpty
    
    // Check if this represents a winning state
    def isWin: Boolean = isTerminal
    
    // Get all possible moves from current state
    def getPossibleMoves: List[PieceAtSlot] = {
      if (possibleMoves.nonEmpty) return possibleMoves
      
      if (remainingPieces.isEmpty) {
        possibleMoves = List.empty
        return possibleMoves
      }
      
      val piece = remainingPieces.head
      val board = Board(m, n, state)
      val availableSlots = board.availableSlots
      val occupiedSlots = state.map(_.slot)
      
      val validMoves = mutable.ListBuffer[PieceAtSlot]()
      
      for (slot <- availableSlots) {
        val attackedSlots = piece.getAttackedSlots(board, slot)
        val attacksExistingPiece = (attackedSlots & occupiedSlots).nonEmpty
        
        if (!attacksExistingPiece) {
          // Check if existing pieces attack this slot
          val isAttackedByExisting = state.exists { existingPiece =>
            val existingAttacks = existingPiece.piece.getAttackedSlots(board, existingPiece.slot)
            existingAttacks.contains(slot)
          }
          
          if (!isAttackedByExisting) {
            validMoves += PieceAtSlot(piece, slot)
          }
        }
      }
      
      possibleMoves = validMoves.toList
      possibleMoves
    }
    
    // Select best child using UCB1
    def selectBestChild: MCTSNode = {
      children.values.maxBy(_.ucb1Score)
    }
    
    // Expand node by adding one new child
    def expand(): Option[MCTSNode] = {
      val moves = getPossibleMoves
      val unexploredMoves = moves.filterNot(children.contains)
      
      if (unexploredMoves.isEmpty) {
        isFullyExpanded = true
        return None
      }
      
      val move = unexploredMoves.head
      val newState = state + move
      val newRemainingPieces = remainingPieces.tail
      val childNode = new MCTSNode(newState, newRemainingPieces, Some(this))
      
      children(move) = childNode
      
      if (unexploredMoves.length == 1) {
        isFullyExpanded = true
      }
      
      Some(childNode)
    }
    
    // Random rollout from current state
    def rollout(): Boolean = {
      var currentState = state
      var currentPieces = remainingPieces
      
      while (currentPieces.nonEmpty) {
        val piece = currentPieces.head
        val board = Board(m, n, currentState)
        val availableSlots = board.availableSlots.toVector
        val occupiedSlots = currentState.map(_.slot)
        
        // Find valid placements
        val validPlacements = mutable.ListBuffer[Slot]()
        
        for (slot <- availableSlots) {
          val attackedSlots = piece.getAttackedSlots(board, slot)
          val attacksExistingPiece = (attackedSlots & occupiedSlots).nonEmpty
          
          if (!attacksExistingPiece) {
            val isAttackedByExisting = currentState.exists { existingPiece =>
              val existingAttacks = existingPiece.piece.getAttackedSlots(board, existingPiece.slot)
              existingAttacks.contains(slot)
            }
            
            if (!isAttackedByExisting) {
              validPlacements += slot
            }
          }
        }
        
        if (validPlacements.isEmpty) {
          return false // No valid placement, rollout fails
        }
        
        // Random selection from valid placements
        val randomSlot = validPlacements(random.nextInt(validPlacements.length))
        currentState = currentState + PieceAtSlot(piece, randomSlot)
        currentPieces = currentPieces.tail
      }
      
      true // Successfully placed all pieces
    }
    
    // Backpropagate result up the tree
    def backpropagate(result: Boolean): Unit = {
      visits += 1
      if (result) wins += 1.0
      
      parent.foreach(_.backpropagate(result))
    }
  }
  
  lazy val solution: Solutions = {
    val solutions = mutable.Set[BoardState]()
    
    // Run MCTS multiple times to find different solutions
    var iteration = 0
    val startTime = System.nanoTime()
    var shouldContinue = true
    
    while (iteration < maxIterations && shouldContinue) {
      val rootNode = new MCTSNode(Set.empty, pieceList)
      val foundSolution = runMCTSIteration(rootNode)
      
      if (foundSolution.nonEmpty) {
        solutions ++= foundSolution
        
        // Early exit if we've found a reasonable number of solutions
        if (solutions.size >= 100 && iteration > 1000) {
          println(s"MCTS: Found ${solutions.size} solutions after $iteration iterations")
          shouldContinue = false
        }
      }
      
      iteration += 1
      
      if (iteration % 1000 == 0) {
        val elapsed = (System.nanoTime() - startTime) / 1000000.0
        println(s"MCTS: $iteration iterations, ${solutions.size} solutions found, ${elapsed}ms elapsed")
      }
    }
    
    val totalTime = (System.nanoTime() - startTime) / 1000000.0
    println(s"MCTS completed: $iteration total iterations, ${solutions.size} unique solutions, ${totalTime}ms")
    
    solutions.toSet
  }
  
  def count: Int = solution.size
  
  private def runMCTSIteration(rootNode: MCTSNode): Set[BoardState] = {
    val solutions = mutable.Set[BoardState]()
    
    // Run MCTS for a limited number of steps per iteration
    for (_ <- 1 to 100) {
      var currentNode = rootNode
      
      // Selection phase - traverse down to leaf
      while (currentNode.isFullyExpanded && currentNode.children.nonEmpty) {
        currentNode = currentNode.selectBestChild
      }
      
      // Expansion phase
      if (!currentNode.isTerminal) {
        currentNode.expand() match {
          case Some(newChild) => currentNode = newChild
          case None => // Node was already fully expanded
        }
      }
      
      // Simulation (rollout) phase
      val rolloutResult = if (currentNode.isTerminal) {
        if (currentNode.isWin) {
          solutions += currentNode.state
          true
        } else {
          false
        }
      } else {
        currentNode.rollout()
      }
      
      // Backpropagation phase
      currentNode.backpropagate(rolloutResult)
    }
    
    solutions.toSet
  }
  
  private def flatPieces(pieces: InitialPieces): List[Piece] = {
    val pieceList = for {
      (p, n) <- pieces.list
      _ <- 1 to n
    } yield p
    pieceList.toList
  }
}