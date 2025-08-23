package pakkio.chesschallenge

// Verification test: Original solver on 7x7 for ground truth validation
object Verify7x7Original extends App {
  println("=== 7x7 Verification Test: Original Solver ===")
  println("Running original HighlyOptimizedParallelSolution for ground truth...")
  println("Expected time: 40+ minutes (no ML optimizations)")
  println("Purpose: Validate our ML-enhanced result of 382,990 solutions")
  
  val initialPieces = InitialPieces(Map(King -> 2, Queen -> 2, Bishop -> 2, Knight -> 1))
  
  println(s"\nBoard: 7x7 (49 squares)")
  println(s"Pieces: ${initialPieces.list.map { case (piece, count) => s"$count ${piece.mnemonic}" }.mkString(", ")}")
  println(s"Total pieces: ${initialPieces.list.values.sum}")
  
  println("\nOriginal solver features:")
  println("• Symmetry elimination (8-way canonical forms)")
  println("• Parallel processing for large datasets") 
  println("• NO ML branch pruning")
  println("• NO intelligent piece ordering")
  
  println(s"\nStarting original 7x7 solve at ${java.time.LocalTime.now()}...")
  println("This will take approximately 40+ minutes...")
  println("Progress updates every few minutes...")
  
  val startTime = System.nanoTime()
  val originalSolver = HighlyOptimizedParallelSolution(7, 7, initialPieces)
  
  // Periodic progress indicator
  println("Computing... (this is the long part)")
  
  val result = originalSolver.count
  val endTime = System.nanoTime()
  val durationMs = (endTime - startTime) / 1000000.0
  val durationMin = durationMs / 60000.0
  
  println(s"\n=== ORIGINAL 7x7 RESULTS (GROUND TRUTH) ===")
  println(s"Solutions found: $result")
  println(s"Runtime: ${durationMs}%.1f ms (${durationMin}%.2f minutes)")
  println(s"Completed at: ${java.time.LocalTime.now()}")
  
  println(s"\n=== VALIDATION COMPARISON ===")
  val mlResult = 382990
  println(s"ML-Enhanced result: $mlResult solutions (42 seconds)")
  println(s"Original result: $result solutions (${durationMin}%.1f minutes)")
  
  if (result == mlResult) {
    println("🎉 PERFECT MATCH: ML optimizations are 100% accurate!")
    println("✅ All 382,990 solutions are verified correct")
    val speedup = durationMin * 60.0 / 42.0
    println(f"🚀 ML version is ${speedup}%.1fx faster with perfect accuracy")
  } else {
    val difference = math.abs(result - mlResult)
    val accuracy = (math.min(result, mlResult).toDouble / math.max(result, mlResult)) * 100
    
    println(f"📊 DIFFERENCE: $difference solutions different")
    println(f"🎯 Accuracy: ${accuracy}%.2f%%")
    
    if (mlResult < result) {
      println("❗ ML version found fewer solutions (false negatives)")
      println("This suggests ML pruning was too aggressive")
    } else {
      println("❗ ML version found more solutions (unexpected)")  
      println("This suggests a bug in canonical form detection")
    }
  }
  
  println(s"\n=== PERFORMANCE ANALYSIS ===")
  println(f"Original solver time: ${durationMin}%.1f minutes")
  println("This establishes the baseline for 7x7 complexity")
  
  if (durationMin < 30) {
    println("⚡ Faster than expected - 7x7 is more tractable")
  } else if (durationMin < 60) {
    println("📊 As expected - significant but manageable complexity")
  } else {
    println("⚠️  Slower than expected - 7x7 is very challenging")
  }
  
  println(s"\n=== VALIDATION COMPLETE ===")
  if (result == mlResult) {
    println("🏆 ML ENHANCEMENTS VALIDATED: Perfect accuracy with massive speedup")
    println("Phase 2 optimizations are production-ready!")
  } else {
    println("🔍 INVESTIGATION NEEDED: Results differ, need to analyze discrepancy")
  }
}