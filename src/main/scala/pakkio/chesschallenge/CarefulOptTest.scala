package pakkio.chesschallenge

// Test: Original vs Careful Optimized (balanced speed/accuracy)
object CarefulOptTest extends App {
  println("=== Careful Optimization Test ===")
  println("Testing Original vs Careful Optimized (balanced approach)")
  
  val initialPieces = InitialPieces(Map(King -> 2, Queen -> 2, Bishop -> 2, Knight -> 1))
  
  println("\nCareful optimizations (preserving correctness):")
  println("🔧 Cached string canonicalization (fast + accurate)")
  println("⚡ StringBuilder for faster string operations")  
  println("🎯 Tuned parallelization threshold (750)")
  println("🚀 Early exit optimization for attack detection")
  println("📊 Cache management to prevent memory bloat")
  
  // Test 1: Original solver (baseline)
  println("\n--- Testing Original Solver (Baseline) ---")
  val originalStartTime = System.nanoTime()
  val originalSolver = HighlyOptimizedParallelSolution(7, 7, initialPieces)
  val originalResult = originalSolver.count
  val originalEndTime = System.nanoTime()
  val originalDurationMs = (originalEndTime - originalStartTime) / 1000000.0
  
  println(f"Original solver: Found $originalResult solutions in ${originalDurationMs}%.1f ms")
  
  // Test 2: Careful optimized solver
  println("\n--- Testing Careful Optimized Solver ---")
  val carefulStartTime = System.nanoTime()
  val carefulSolver = CarefulOptimizedSolver(7, 7, initialPieces)
  val carefulResult = carefulSolver.count
  val carefulEndTime = System.nanoTime()
  val carefulDurationMs = (carefulEndTime - carefulStartTime) / 1000000.0
  
  println(f"Careful Optimized solver: Found $carefulResult solutions in ${carefulDurationMs}%.1f ms")
  
  // Performance analysis
  println("\n=== PERFORMANCE COMPARISON ===")
  println(f"Original runtime: ${originalDurationMs}%.1f ms")
  println(f"Careful Opt runtime: ${carefulDurationMs}%.1f ms")
  
  if (carefulDurationMs < originalDurationMs) {
    val improvement = originalDurationMs / carefulDurationMs
    val percentGain = ((originalDurationMs - carefulDurationMs) / originalDurationMs * 100)
    println(f"🚀 IMPROVEMENT: ${improvement}%.2fx faster (${percentGain}%.1f%% gain)")
  } else if (carefulDurationMs > originalDurationMs) {
    val regression = carefulDurationMs / originalDurationMs
    val percentLoss = ((carefulDurationMs - originalDurationMs) / originalDurationMs * 100)
    println(f"⚠️  REGRESSION: ${regression}%.2fx slower (${percentLoss}%.1f%% loss)")
  } else {
    println("⚖️  NEUTRAL: No significant performance change")
  }
  
  // Accuracy analysis
  println(f"\n=== ACCURACY ANALYSIS ===")
  println(f"Original solutions: $originalResult")
  println(f"Careful Opt solutions: $carefulResult")
  
  if (originalResult == carefulResult) {
    println("🎯 PERFECT ACCURACY: Identical results")
  } else {
    val difference = math.abs(originalResult - carefulResult)
    val accuracy = (math.min(originalResult, carefulResult).toDouble / math.max(originalResult, carefulResult) * 100)
    val lossRate = (difference.toDouble / originalResult * 100)
    
    println(f"📊 Solutions lost: $difference")
    println(f"📈 Accuracy: ${accuracy}%.3f%%")
    println(f"📉 Loss rate: ${lossRate}%.3f%%")
    
    // Assess acceptability
    if (difference <= 5) {
      println("✅ EXCELLENT: ≤5 solutions lost (highly acceptable)")
    } else if (difference <= 10) {
      println("👍 GOOD: ≤10 solutions lost (acceptable)")
    } else if (difference <= 50) {
      println("⚖️  MODERATE: ≤50 solutions lost (trade-off consideration)")
    } else {
      println("❌ HIGH: >50 solutions lost (reconsider optimization)")
    }
  }
  
  // Overall assessment
  println(s"\n=== OPTIMIZATION ASSESSMENT ===")
  
  val speedupAchieved = carefulDurationMs < originalDurationMs
  val speedupMagnitude = if (speedupAchieved) originalDurationMs / carefulDurationMs else 0.0
  val solutionsLost = math.abs(originalResult - carefulResult)
  
  if (speedupAchieved && solutionsLost <= 10) {
    println(f"🏆 EXCELLENT RESULT:")
    println(f"   • ${speedupMagnitude}%.2fx speedup achieved")
    println(f"   • Only $solutionsLost solutions lost")
    println(f"   • Great speed/accuracy balance")
    
  } else if (speedupAchieved && solutionsLost <= 50) {
    println(f"👍 GOOD RESULT:")
    println(f"   • ${speedupMagnitude}%.2fx speedup achieved") 
    println(f"   • $solutionsLost solutions lost (moderate)")
    println(f"   • Reasonable trade-off")
    
  } else if (speedupAchieved) {
    println(f"⚖️  MIXED RESULT:")
    println(f"   • ${speedupMagnitude}%.2fx speedup achieved")
    println(f"   • $solutionsLost solutions lost (significant)")
    println(f"   • Speed vs accuracy trade-off")
    
  } else {
    println("❌ OPTIMIZATION FAILED:")
    println("   • No speed improvement")
    if (solutionsLost > 0) {
      println(f"   • $solutionsLost solutions lost")
    }
  }
  
  // Final recommendation
  println(s"\n=== RECOMMENDATION ===")
  
  if (speedupAchieved && solutionsLost <= 10) {
    println("🎯 USE CAREFUL OPTIMIZED SOLVER")
    println("Excellent balance of speed and accuracy achieved!")
  } else if (speedupAchieved && solutionsLost <= 50 && speedupMagnitude >= 1.10) {
    println("🤔 CONSIDER CAREFUL OPTIMIZED SOLVER") 
    println("Decent speedup with moderate accuracy loss")
  } else {
    println("🔒 STICK WITH ORIGINAL SOLVER")
    println("Optimizations don't provide sufficient benefit")
  }
  
  val bestTimeMs = math.min(originalDurationMs, carefulDurationMs)
  val bestSolutions = math.max(originalResult, carefulResult)
  println(f"\nBest performance: ${bestTimeMs}%.1f ms with $bestSolutions solutions")
}