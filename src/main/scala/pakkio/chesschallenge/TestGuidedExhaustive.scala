package pakkio.chesschallenge

object TestGuidedExhaustive {
  def main(args: Array[String]): Unit = {
    println("=== Guided Exhaustive vs Current Solver Benchmark ===")
    
    val initialPieces = InitialPieces(Map(King -> 2, Queen -> 2, Bishop -> 2, Knight -> 1))
    
    // Test on 6x6 first to validate coverage
    println("\n--- 6x6 Board Test (Coverage Validation) ---")
    test6x6(initialPieces)
    
    // Test on 7x7 for speed comparison
    println("\n--- 7x7 Board Test (Speed Comparison) ---")
    test7x7(initialPieces)
  }
  
  def test6x6(initialPieces: InitialPieces): Unit = {
    println("Testing coverage: Guided Exhaustive vs CarefulOptimizedSolver on 6x6...")
    
    // Baseline: Current solver
    println("Running CarefulOptimizedSolver...")
    val startTime1 = System.nanoTime()
    val currentSolver = CarefulOptimizedSolver(6, 6, initialPieces)
    val currentResult = currentSolver.count
    val currentTime = (System.nanoTime() - startTime1) / 1000000.0
    
    println(s"CarefulOptimizedSolver: $currentResult solutions in ${currentTime}ms")
    
    // Test Guided Exhaustive solver
    println("Running GuidedExhaustiveSolver...")
    val startTime2 = System.nanoTime()
    val guidedSolver = GuidedExhaustiveSolver(6, 6, initialPieces)
    val guidedResult = guidedSolver.count
    val guidedTime = (System.nanoTime() - startTime2) / 1000000.0
    
    println(s"GuidedExhaustiveSolver: $guidedResult solutions in ${guidedTime}ms")
    
    // Compare results
    val speedRatio = if (guidedTime > 0) currentTime / guidedTime else 0
    val coverage = guidedResult.toDouble / currentResult * 100
    
    println(f"\n=== 6x6 Results ===")
    println(f"Solution coverage: ${coverage}%.1f%% of expected solutions")
    println(f"Speed comparison: ${speedRatio}%.2fx ${if (speedRatio > 1) "faster" else "slower"} (Guided)")
    
    if (coverage >= 90.0) {
      println("✅ EXCELLENT: 90%+ solution coverage achieved!")
      if (speedRatio > 1.0) {
        println("🚀 BONUS: Also faster than current solver!")
      } else if (speedRatio > 0.5) {
        println("⚡ GOOD: Competitive speed with better coverage")
      }
    } else if (coverage >= 50.0) {
      println("⚡ GOOD: Significant improvement over Smart MCTS")
      println("🔧 May need further optimization for 90% target")
    } else {
      println("⏳ NEEDS WORK: Still missing too many solutions")
    }
  }
  
  def test7x7(initialPieces: InitialPieces): Unit = {
    println("Testing speed: Guided Exhaustive on 7x7...")
    
    // Test Guided Exhaustive solver on 7x7
    println("Running GuidedExhaustiveSolver on 7x7...")
    val startTime = System.nanoTime()
    val guidedSolver = GuidedExhaustiveSolver(7, 7, initialPieces)
    val guidedResult = guidedSolver.count
    val guidedTime = (System.nanoTime() - startTime) / 1000000.0
    
    println(s"GuidedExhaustiveSolver: $guidedResult solutions in ${guidedTime}ms (${guidedTime/1000}s)")
    
    // Compare with known baselines
    val knownBaselineMs = 14400.0 // 14.4s current best
    val speedup = knownBaselineMs / guidedTime
    
    println(f"\n=== 7x7 Results ===")
    println(f"Comparison with known 7x7 baseline (14.4s):")
    println(f"Guided Exhaustive speedup: ${speedup}%.2fx ${if (speedup > 1) "faster" else "slower"}")
    
    if (guidedTime < knownBaselineMs) {
      println("🎉 Guided Exhaustive beats the 14.4s baseline!")
      if (guidedTime < 10000) {
        println("🚀 Achieves sub-10 second 7x7 solution!")
      }
      if (guidedTime < 5000) {
        println("⚡ Achieves sub-5 second 7x7 solution!")
      }
    } else if (guidedTime < knownBaselineMs * 2) {
      println("⚡ Guided Exhaustive is competitive with current approach")
    } else {
      println("⏳ Guided Exhaustive needs optimization for 7x7")
    }
    
    println(s"Solutions found: $guidedResult")
    
    // Estimate coverage if we had a baseline
    println(f"Expected to find high percentage of total solutions (unlike Smart MCTS)")
  }
}