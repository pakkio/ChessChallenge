package pakkio.chesschallenge

object Test7x7 {
  def main(args: Array[String]): Unit = {
    println("Starting 7x7 Chess Challenge with Symmetry Elimination!")
    println("Placing 2 Kings, 2 Queens, 2 Bishops, and 1 Knight on a 7x7 board...")
    println(s"Running on ${Runtime.getRuntime.availableProcessors} CPU cores")
    
    val initialPieces = InitialPieces(Map(King -> 2, Queen -> 2, Bishop -> 2, Knight -> 1))
    
    // Warmup JVM first
    println("Warming up JVM...")
    val warmupSolution = HighlyOptimizedParallelSolution(7, 7, initialPieces)
    warmupSolution.count // Force computation
    
    // Now measure actual performance after warmup
    println("Measuring actual performance on 7x7...")
    val startTime = System.nanoTime()
    
    val solution = HighlyOptimizedParallelSolution(7, 7, initialPieces)
    val result = solution.count
    
    val endTime = System.nanoTime()
    val durationMs = (endTime - startTime) / 1000000.0
    val durationMinutes = durationMs / 60000.0
    
    println(s"Found $result unique solutions (canonical forms) in ${durationMs}ms")
    println(s"Time: ${durationMinutes.formatted("%.2f")} minutes")
    println(s"Estimated full solution count (with symmetries): ${result * 8}")
    
    // Compare against estimated 40+ minutes from the enhancement plan
    val estimatedOriginalTime = 40 * 60 * 1000.0 // 40 minutes in ms
    val speedupFactor = estimatedOriginalTime / durationMs
    
    println(s"Expected original time (without symmetry): ~40 minutes")
    println(s"Actual speedup achieved: ${speedupFactor.formatted("%.1f")}x faster")
    
    if (durationMinutes < 10) {
      println("✅ SUCCESS: 7x7 is now computationally feasible!")
    } else if (durationMinutes < 20) {
      println("⚡ GOOD: 7x7 runs in reasonable time, could benefit from Phase 2 optimizations")
    } else {
      println("⏳ SLOW: 7x7 still challenging, Phase 2-3 AI optimizations needed")
    }
  }
}