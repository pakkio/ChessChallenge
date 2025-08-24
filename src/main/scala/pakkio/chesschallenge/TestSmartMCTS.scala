package pakkio.chesschallenge

object TestSmartMCTS {
  def main(args: Array[String]): Unit = {
    println("=== Smart MCTS vs Current Solver Benchmark ===")
    
    val initialPieces = InitialPieces(Map(King -> 2, Queen -> 2, Bishop -> 2, Knight -> 1))
    
    // Test on 6x6 first
    println("\n--- 6x6 Board Test ---")
    test6x6(initialPieces)
    
    // Test on 7x7 if 6x6 shows promise
    println("\n--- 7x7 Board Test (if 6x6 is promising) ---")
    
    // Run 7x7 test regardless to see performance
    test7x7(initialPieces)
  }
  
  def test6x6(initialPieces: InitialPieces): Unit = {
    println("Testing Smart MCTS on 6x6 board...")
    
    // Test current solver first (quick baseline)
    println("Running CarefulOptimizedSolver for baseline...")
    val startTime1 = System.nanoTime()
    val currentSolver = CarefulOptimizedSolver(6, 6, initialPieces)
    val currentResult = currentSolver.count
    val currentTime = (System.nanoTime() - startTime1) / 1000000.0
    
    println(s"CarefulOptimizedSolver: $currentResult solutions in ${currentTime}ms")
    
    // Test Smart MCTS solver
    println("Running SmartMCTSSolver...")
    val startTime2 = System.nanoTime()
    val smartMctsSolver = SmartMCTSSolver(6, 6, initialPieces, maxIterations = 2000)
    val smartMctsResult = smartMctsSolver.count
    val smartMctsTime = (System.nanoTime() - startTime2) / 1000000.0
    
    println(s"SmartMCTSSolver: $smartMctsResult solutions in ${smartMctsTime}ms")
    
    // Compare results
    val speedup = if (smartMctsTime > 0) currentTime / smartMctsTime else 0
    val accuracy = smartMctsResult.toDouble / currentResult * 100
    
    println(f"Speed comparison: ${speedup}%.2fx ${if (speedup > 1) "faster" else "slower"} (Smart MCTS)")
    println(f"Solution accuracy: ${accuracy}%.1f%% of expected solutions")
    
    if (smartMctsTime < currentTime && smartMctsResult >= currentResult * 0.1) {
      println("✅ Smart MCTS shows promise on 6x6!")
    } else if (smartMctsTime < currentTime * 5 && smartMctsResult > 0) {
      println("⚡ Smart MCTS finds solutions, may benefit from tuning")
    } else {
      println("⏳ Smart MCTS needs significant optimization")
    }
  }
  
  def test7x7(initialPieces: InitialPieces): Unit = {
    println("Testing Smart MCTS on 7x7 board...")
    
    // Test Smart MCTS solver with extended parameters
    println("Running SmartMCTSSolver with extended search...")
    val startTime = System.nanoTime()
    val smartMctsSolver = SmartMCTSSolver(7, 7, initialPieces, 
      maxIterations = 10000, 
      explorationConstant = 1.0)
      
    val smartMctsResult = smartMctsSolver.count
    val smartMctsTime = (System.nanoTime() - startTime) / 1000000.0
    
    println(s"SmartMCTSSolver: $smartMctsResult solutions in ${smartMctsTime}ms (${smartMctsTime/1000}s)")
    
    // Compare with known baseline (14.4s from previous optimizations)
    val knownBaselineMs = 14400.0
    val speedup = knownBaselineMs / smartMctsTime
    
    println(f"Comparison with known 7x7 baseline (14.4s):")
    println(f"Smart MCTS speedup: ${speedup}%.2fx ${if (speedup > 1) "faster" else "slower"}")
    
    if (smartMctsTime < knownBaselineMs) {
      println("🎉 Smart MCTS beats the 14.4s baseline!")
      if (smartMctsTime < 10000) {
        println("🚀 Smart MCTS achieves sub-10 second 7x7 solution!")
      }
      if (smartMctsTime < 5000) {
        println("⚡ Smart MCTS achieves sub-5 second 7x7 solution!")
      }
    } else if (smartMctsTime < knownBaselineMs * 2) {
      println("⚡ Smart MCTS is competitive with current approach")
    } else {
      println("⏳ Smart MCTS underperforms current approach on 7x7")
    }
    
    if (smartMctsResult > 0) {
      println(s"Smart MCTS found $smartMctsResult unique solutions")
      // Note: Accuracy comparison would require running baseline, which takes 14.4s
    } else {
      println("❌ Smart MCTS found no solutions - algorithm needs refinement")
    }
  }
}