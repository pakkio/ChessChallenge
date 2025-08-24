package pakkio.chesschallenge

object TestNative {
  def main(args: Array[String]): Unit = {
    println("=== Phase 4: GraalVM Native Performance Test ===")
    println("Testing native-compatible solver on 7x7...")
    
    val initialPieces = InitialPieces(Map(King -> 2, Queen -> 2, Bishop -> 2, Knight -> 1))
    
    val startTime = System.nanoTime()
    val solver = NativeCompatibleSolver(7, 7, initialPieces)
    val result = solver.count
    val totalTime = (System.nanoTime() - startTime) / 1000000.0
    
    println(s"Native Compatible Solver: $result solutions in ${totalTime}ms (${totalTime/1000}s)")
    
    val baselineTime = 14400.0 // 14.4s
    val speedup = baselineTime / totalTime
    
    println(f"vs 14.4s JVM baseline: ${speedup}%.2fx ${if (speedup > 1) "faster" else "slower"}")
    
    if (totalTime < baselineTime) {
      println("🎉 Phase 4 SUCCESS: Native compilation beats JVM baseline!")
    } else {
      println("⏳ Native compilation doesn't improve over JVM baseline")
    }
  }
}