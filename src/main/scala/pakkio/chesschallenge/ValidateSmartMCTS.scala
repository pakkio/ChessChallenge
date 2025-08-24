package pakkio.chesschallenge

object ValidateSmartMCTS {
  def main(args: Array[String]): Unit = {
    println("=== Validating Smart MCTS Solutions ===")
    
    val initialPieces = InitialPieces(Map(King -> 2, Queen -> 2, Bishop -> 2, Knight -> 1))
    
    // Test on smaller board first to validate correctness
    println("\n--- Validating 6x6 Solutions ---")
    val smartSolver = SmartMCTSSolver(6, 6, initialPieces, maxIterations = 100)
    val solutions = smartSolver.solution
    
    println(s"Found ${solutions.size} solutions, validating each...")
    
    var validSolutions = 0
    var invalidSolutions = 0
    
    for ((solution, index) <- solutions.zipWithIndex) {
      if (isValidSolution(solution, 6, 6)) {
        validSolutions += 1
        if (index < 3) { // Print first 3 solutions for inspection
          println(s"Solution ${index + 1}: ${formatSolution(solution)}")
        }
      } else {
        invalidSolutions += 1
        println(s"❌ Invalid solution found: ${formatSolution(solution)}")
      }
    }
    
    println(s"\nValidation Results:")
    println(s"Valid solutions: $validSolutions")
    println(s"Invalid solutions: $invalidSolutions")
    println(s"Correctness: ${if (invalidSolutions == 0) "✅ ALL SOLUTIONS VALID" else s"❌ ${invalidSolutions} invalid solutions"}")
    
    if (invalidSolutions == 0 && validSolutions > 0) {
      println("\n✅ Smart MCTS produces correct solutions!")
      println("The algorithm trades solution completeness for speed - it finds some valid solutions very quickly.")
      println("This could be useful for:")
      println("- Quick solution sampling")
      println("- Real-time puzzle generation")
      println("- Monte Carlo estimation of solution space size")
    }
  }
  
  def isValidSolution(solution: Set[PieceAtSlot], m: Int, n: Int): Boolean = {
    val board = Board(m, n, Set.empty)
    
    // Check piece counts
    val pieceCounts = solution.groupBy(_.piece).mapValues(_.size)
    val expectedCounts = Map(King -> 2, Queen -> 2, Bishop -> 2, Knight -> 1)
    
    if (pieceCounts != expectedCounts) {
      println(s"Wrong piece counts: expected $expectedCounts, got $pieceCounts")
      return false
    }
    
    // Check positions are on board
    if (solution.exists(p => !board.isValidPosition(p.slot))) {
      println("Some pieces are off the board")
      return false
    }
    
    // Check no two pieces on same slot
    val slots = solution.map(_.slot)
    if (slots.size != solution.size) {
      println("Multiple pieces on same slot")
      return false
    }
    
    // Check no piece attacks another
    for (piece1 <- solution) {
      val attackedSlots = piece1.piece.getAttackedSlots(board, piece1.slot)
      val attackedPieces = solution.filter(p => attackedSlots.contains(p.slot) && p != piece1)
      
      if (attackedPieces.nonEmpty) {
        println(s"${piece1.piece} at ${piece1.slot} attacks ${attackedPieces.map(p => s"${p.piece} at ${p.slot}").mkString(", ")}")
        return false
      }
    }
    
    true
  }
  
  def formatSolution(solution: Set[PieceAtSlot]): String = {
    solution.toList.sortBy(p => (p.slot.x, p.slot.y))
      .map(p => s"${p.piece.mnemonic}${p.slot.x}${p.slot.y}")
      .mkString(",")
  }
}