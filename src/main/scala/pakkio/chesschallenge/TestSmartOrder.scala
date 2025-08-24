package pakkio.chesschallenge

object TestSmartOrder {
  def main(args: Array[String]): Unit = {
    println("=== Smart Order Test: Minimal Change for Maximum Gain ===")
    
    val initialPieces = InitialPieces(Map(King -> 2, Queen -> 2, Bishop -> 2, Knight -> 1))
    
    // Quick test on 6x6
    test6x6(initialPieces)
    
    // If 6x6 looks good, test 7x7
    println("\n--- 7x7 Test ---")
    test7x7(initialPieces)
  }
  
  def test6x6(initialPieces: InitialPieces): Unit = {
    println("--- 6x6 Quick Validation ---")
    
    // Original order
    println("Original CarefulOptimizedSolver...")
    val startTime1 = System.nanoTime()
    val originalSolver = CarefulOptimizedSolver(6, 6, initialPieces)
    val originalResult = originalSolver.count
    val originalTime = (System.nanoTime() - startTime1) / 1000000.0
    
    // Smart order
    println("Smart Order Solver...")
    val startTime2 = System.nanoTime()
    val smartSolver = SmartOrderSolver(6, 6, initialPieces)
    val smartResult = smartSolver.count
    val smartTime = (System.nanoTime() - startTime2) / 1000000.0
    
    val speedup = if (smartTime > 0) originalTime / smartTime else 0
    
    println(f"\n6x6 Results:")
    println(f"Original: $originalResult solutions, ${originalTime}ms")  
    println(f"Smart Order: $smartResult solutions, ${smartTime}ms")
    println(f"Speedup: ${speedup}%.2fx ${if (speedup > 1) "faster" else "slower"}")
    println(f"Coverage: ${smartResult == originalResult}")
  }
  
  def test7x7(initialPieces: InitialPieces): Unit = {
    println("Testing Smart Order on 7x7...")
    
    val startTime = System.nanoTime()
    val smartSolver = SmartOrderSolver(7, 7, initialPieces)
    val smartResult = smartSolver.count
    val smartTime = (System.nanoTime() - startTime) / 1000000.0
    
    val baseline = 14400.0 // 14.4s
    val speedup = baseline / smartTime
    
    println(f"\n7x7 Results:")
    println(f"Smart Order: $smartResult solutions in ${smartTime}ms (${smartTime/1000}s)")
    println(f"vs 14.4s baseline: ${speedup}%.2fx ${if (speedup > 1) "faster" else "slower"}")
    
    if (smartTime < baseline) {
      println("🎉 SUCCESS: Beats 14.4s baseline with simple piece reordering!")
    } else if (smartTime < baseline * 1.2) {
      println("✅ CLOSE: Very competitive with baseline")
    } else {
      println("⏳ Piece ordering alone insufficient for major speedup")
    }
  }
}