package pakkio.chesschallenge

object QuickSATTest {
  def main(args: Array[String]): Unit = {
    println("=== Quick SAT Test (6x6 only) ===")
    
    val initialPieces = InitialPieces(Map(King -> 2, Queen -> 2, Bishop -> 2, Knight -> 1))
    
    println("Running SAT solver on 6x6...")
    val startTime = System.nanoTime()
    val satSolver = SATSolver(6, 6, initialPieces)
    val solutions = satSolver.solution
    val totalTime = (System.nanoTime() - startTime) / 1000000.0
    
    println(f"SAT Solver: ${solutions.size} solutions in ${totalTime}ms")
    
    if (solutions.nonEmpty) {
      println("First solution:")
      val firstSolution = solutions.head
      for (piece <- firstSolution.toList.sortBy(p => (p.slot.x, p.slot.y))) {
        println(s"  ${piece.piece.mnemonic} at (${piece.slot.x}, ${piece.slot.y})")
      }
    }
    
    println(f"Expected for 6x6: 2,969 canonical solutions")
    println(f"Found: ${solutions.size} solutions")
    
    if (solutions.size == 2969) {
      println("✅ PERFECT: SAT solver finds all 6x6 solutions!")
    } else if (solutions.size == 1) {
      println("⚠️  SAT found one solution (single solution mode)")
    } else if (solutions.size > 0) {
      println(s"⚡ SAT found ${solutions.size} solutions (partial enumeration)")
    } else {
      println("❌ No solutions found")
    }
  }
}