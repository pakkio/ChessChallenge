package pakkio.chesschallenge

object QuickTest {
  def main(args: Array[String]): Unit = {
    println("=== Quick Thread Safety Test ===")
    
    val initialPieces = InitialPieces(Map(King -> 1, Queen -> 1))  // Smaller test
    
    println("Testing with 1 King + 1 Queen on 4x4...")
    val startTime = System.nanoTime()
    val solver = BestSolutionSoFar(4, 4, initialPieces)
    val result = solver.count
    val totalTime = (System.nanoTime() - startTime) / 1000000.0
    
    println(s"Result: $result solutions in ${totalTime}ms")
    
    if (totalTime < 5000) {
      println("✅ Quick test completed successfully")
    } else {
      println("❌ Test took too long - potential infinite loop or race condition")
    }
  }
}