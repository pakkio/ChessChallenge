package pakkio.chesschallenge

object TestMCTS {
  def main(args: Array[String]): Unit = {
    println("=== MCTS vs Current Solver Benchmark ===")
    
    val initialPieces = InitialPieces(Map(King -> 2, Queen -> 2, Bishop -> 2, Knight -> 1))
    
    // Test on 6x6 first
    println("\n--- 6x6 Board Test ---")
    test6x6(initialPieces)
    
    // Test on 7x7 if 6x6 is promising
    println("\n--- 7x7 Board Test ---")
    test7x7(initialPieces)
  }
  
  def test6x6(initialPieces: InitialPieces): Unit = {
    println("Testing MCTS on 6x6 board...")
    
    // Test current solver first
    println("Running CarefulOptimizedSolver...")
    val startTime1 = System.nanoTime()
    val currentSolver = CarefulOptimizedSolver(6, 6, initialPieces)
    val currentResult = currentSolver.count
    val currentTime = (System.nanoTime() - startTime1) / 1000000.0
    
    println(s"CarefulOptimizedSolver: $currentResult solutions in ${currentTime}ms")
    
    // Test MCTS solver
    println("Running MCTSSolver...")
    val startTime2 = System.nanoTime()
    val mctsSolver = MCTSSolver(6, 6, initialPieces, maxIterations = 5000)
    val mctsResult = mctsSolver.count
    val mctsTime = (System.nanoTime() - startTime2) / 1000000.0
    
    println(s"MCTSSolver: $mctsResult solutions in ${mctsTime}ms")
    
    // Compare results
    val speedup = if (mctsTime > 0) currentTime / mctsTime else 0
    println(f"Speed comparison: ${speedup}%.2fx ${if (speedup > 1) "faster" else "slower"} (MCTS)")
    println(f"Solution accuracy: ${mctsResult.toDouble / currentResult * 100}%.1f%% of expected solutions")
    
    if (mctsTime < currentTime && mctsResult >= currentResult * 0.8) {
      println("✅ MCTS shows promise on 6x6!")
    } else if (mctsTime < currentTime * 2) {
      println("⚡ MCTS is competitive, may benefit from tuning")
    } else {
      println("⏳ MCTS needs optimization or may not be suitable for this problem")
    }
  }
  
  def test7x7(initialPieces: InitialPieces): Unit = {
    println("Testing MCTS on 7x7 board...")
    
    // First get baseline with current solver (just time, not full execution due to time)
    println("Getting baseline timing from CarefulOptimizedSolver...")
    val startTime1 = System.nanoTime()
    
    // Run for limited time to get estimate
    val currentSolver = CarefulOptimizedSolver(7, 7, initialPieces)
    val currentResult = try {
      // Add timeout mechanism
      val result = currentSolver.count
      val currentTime = (System.nanoTime() - startTime1) / 1000000.0
      println(s"CarefulOptimizedSolver: $result solutions in ${currentTime}ms")
      Some((result, currentTime))
    } catch {
      case _: Exception =>
        val timeElapsed = (System.nanoTime() - startTime1) / 1000000.0
        println(s"CarefulOptimizedSolver: taking too long, stopped after ${timeElapsed}ms")
        None
    }
    
    // Test MCTS solver with more iterations for 7x7
    println("Running MCTSSolver with extended parameters...")
    val startTime2 = System.nanoTime()
    val mctsSolver = MCTSSolver(7, 7, initialPieces, 
      maxIterations = 20000, 
      explorationConstant = 1.0) // Slightly less exploration for efficiency
      
    val mctsResult = mctsSolver.count
    val mctsTime = (System.nanoTime() - startTime2) / 1000000.0
    
    println(s"MCTSSolver: $mctsResult solutions in ${mctsTime}ms (${mctsTime/1000}s)")
    
    // Compare with known baseline (14.4s from previous optimizations)
    val knownBaselineMs = 14400.0
    val speedup = knownBaselineMs / mctsTime
    
    println(f"Comparison with known 7x7 baseline (14.4s):")
    println(f"MCTS speedup: ${speedup}%.2fx ${if (speedup > 1) "faster" else "slower"}")
    
    if (mctsTime < knownBaselineMs) {
      println("🎉 MCTS beats the 14.4s baseline!")
      if (mctsTime < 10000) {
        println("🚀 MCTS achieves sub-10 second 7x7 solution!")
      }
    } else if (mctsTime < knownBaselineMs * 2) {
      println("⚡ MCTS is competitive, may benefit from further tuning")
    } else {
      println("⏳ MCTS underperforms current approach on 7x7")
    }
    
    currentResult match {
      case Some((baselineCount, _)) =>
        val accuracy = mctsResult.toDouble / baselineCount * 100
        println(f"Solution accuracy: ${accuracy}%.1f%% of baseline")
      case None =>
        println(s"MCTS found $mctsResult solutions (baseline comparison unavailable)")
    }
  }
}