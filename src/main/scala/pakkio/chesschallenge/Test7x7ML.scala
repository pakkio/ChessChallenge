package pakkio.chesschallenge

// 7x7 test using CarefulOptimizedSolver with optimizations
object Test7x7ML extends App {
  println("=== 7x7 Chess Challenge with Careful Optimizations ===")
  println("Testing CarefulOptimizedSolver on 7x7 board...")
  println("Expected time: ~38 seconds (optimized)")
  
  val initialPieces = InitialPieces(Map(King -> 2, Queen -> 2, Bishop -> 2, Knight -> 1))
  
  println(s"Board: 7x7 (49 squares)")
  println(s"Pieces: ${initialPieces.list.map { case (piece, count) => s"$count ${piece.mnemonic}" }.mkString(", ")}")
  println(s"Total pieces: ${initialPieces.list.values.sum}")
  println(s"Available squares after placement: ${49 - initialPieces.list.values.sum}")
  
  println("\nOptimizations active:")
  println("✅ Cached string canonicalization")
  println("✅ StringBuilder for faster string operations")
  println("✅ 8-way symmetry elimination") 
  println("✅ Parallel processing for large datasets")
  
  println(s"\nStarting 7x7 solve at ${java.time.LocalTime.now()}...")
  println("Progress will be shown via cache statistics...")
  
  val startTime = System.nanoTime()
  val solver = CarefulOptimizedSolver(7, 7, initialPieces)
  
  // Start timing
  val result = solver.count
  val endTime = System.nanoTime()
  val durationMs = (endTime - startTime) / 1000000.0
  val durationMin = durationMs / 60000.0
  
  println(s"\n=== 7x7 RESULTS ===")
  println(s"Solutions found: $result")
  println(s"Runtime: ${durationMs}%.1f ms (${durationMin}%.2f minutes)")
  println(s"Completed at: ${java.time.LocalTime.now()}")
  
  // Performance analysis
  val expectedTimeS = 38.4
  val actualSpeedup = if (durationMs < expectedTimeS * 1000) (expectedTimeS * 1000) / durationMs else 0.0
  
  if (durationMs < 45000) {
    println(f"🎉 SUCCESS: Completed in ${durationMs}%.1f ms!")
    if (actualSpeedup > 1.0) {
      println(f"🚀 ${actualSpeedup}%.2fx faster than expected ${expectedTimeS}s")
    }
    println("Careful optimizations working well!")
  } else if (durationMs < 60000) {
    println(f"✅ GOOD: Completed in ${durationMs}%.1f ms")  
    println("Within expected performance range")
  } else {
    println(f"⚠️  SLOWER: Took ${durationMs}%.1f ms")
    println("May need further optimization")
  }
  
  // Expected solutions validation
  val expectedSolutions = 382990
  if (result == expectedSolutions) {
    println(f"✅ ACCURACY: Perfect match - ${result} solutions")
  } else {
    val difference = math.abs(result - expectedSolutions)
    println(f"⚠️  ACCURACY: ${result} vs expected ${expectedSolutions} (diff: ${difference})")
  }
  
  println("\n=== CAREFUL OPTIMIZATION ASSESSMENT ===")
  println("Optimizations active:")
  println("• Cached string canonicalization: Fast symmetry detection")
  println("• StringBuilder optimization: Reduced string allocation overhead") 
  println("• Tuned parallelization: Optimal threshold for parallel processing")
  println("• Early exit patterns: Reduced unnecessary computations")
  
  if (durationMs <= 45000 && result == expectedSolutions) {
    println("\n🏆 OPTIMIZATION SUCCESS: Fast execution with perfect accuracy!")
  }
}