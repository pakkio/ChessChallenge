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
    // 8 x canonical is only an upper bound: symmetric solutions have orbits smaller than 8
    // (on 7x7, 23 orbits of size 4), so the exact total is 3,063,828. See OrbitSpec.
    println(s"Upper bound on the full solution count (8 x canonical): ${result * 8}")

    // The original has never been timed on 7x7: 40 minutes is an ESTIMATE from the enhancement
    // plan, so the ratio below is indicative only and must not be quoted as a measured speedup.
    val estimatedOriginalTime = 40 * 60 * 1000.0 // 40 minutes in ms
    val speedupFactor = estimatedOriginalTime / durationMs

    println(s"Estimated original time (NOT measured): ~40 minutes")
    println(s"Ratio against that estimate (indicative only): ${speedupFactor.formatted("%.1f")}x")
    
    if (durationMinutes < 10) {
      println("✅ SUCCESS: 7x7 is now computationally feasible!")
    } else if (durationMinutes < 20) {
      println("⚡ GOOD: 7x7 runs in reasonable time, could benefit from Phase 2 optimizations")
    } else {
      println("⏳ SLOW: 7x7 still challenging, Phase 2-3 AI optimizations needed")
    }
  }
}