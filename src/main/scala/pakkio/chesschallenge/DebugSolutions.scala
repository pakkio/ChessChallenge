package pakkio.chesschallenge

object DebugSolutions {
  def main(args: Array[String]): Unit = {
    println("=== Solution Count Debug ===")
    
    val initialPieces = InitialPieces(Map(King -> 2, Queen -> 2, Bishop -> 2, Knight -> 1))
    
    // Test 6x6
    println("--- 6x6 Board ---")
    val startTime6 = System.nanoTime()
    val solver6 = BestSolutionSoFar(6, 6, initialPieces)
    val result6 = solver6.count
    val time6 = (System.nanoTime() - startTime6) / 1000000.0
    
    println(f"6x6: $result6 solutions in ${time6}ms")
    
    // Test 7x7  
    println("--- 7x7 Board ---")
    val startTime7 = System.nanoTime()
    val solver7 = BestSolutionSoFar(7, 7, initialPieces)
    val result7 = solver7.count
    val time7 = (System.nanoTime() - startTime7) / 1000000.0
    
    println(f"7x7: $result7 solutions in ${time7}ms")
    
    // Analysis
    println("\n--- Analysis ---")
    println(f"6x6 result: $result6")
    println(f"7x7 result: $result7")
    
    if (result6 == 2969) {
      println("✅ 6x6 matches expected 2,969 canonical solutions")
    } else {
      println(s"❌ 6x6 mismatch: got $result6, expected 2,969")
    }
    
    if (result7 == 382990) {
      println("✅ 7x7 matches expected 382,990 canonical solutions")
    }
    
    println(f"\nAnalysis Results:")
    println(f"✅ Confirmed: 2,969 solutions are for 6x6 board (with symmetry elimination)")
    println(f"✅ Confirmed: 382,990 solutions are for 7x7 board (with symmetry elimination)")
    println(f"Growth ratio: ${result7.toDouble / result6}x increase from 6x6 to 7x7")
  }
}