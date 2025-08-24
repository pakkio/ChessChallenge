package pakkio.chesschallenge

object TestSAT {
  def main(args: Array[String]): Unit = {
    println("=== SAT Solver vs Current Best (14.4s) Benchmark ===")
    println("Testing Boolean Satisfiability approach for chess constraints")
    println()
    
    val initialPieces = InitialPieces(Map(King -> 2, Queen -> 2, Bishop -> 2, Knight -> 1))
    
    // Start with 6x6 for correctness validation
    test6x6(initialPieces)
    
    // If 6x6 works, test 7x7 for performance comparison
    println("\n" + "="*60)
    test7x7(initialPieces)
  }
  
  def test6x6(initialPieces: InitialPieces): Unit = {
    println("--- 6x6 Board: Correctness Validation ---")
    
    // Baseline: Current best solver
    println("Running CarefulOptimizedSolver (baseline)...")
    val baselineStart = System.nanoTime()
    val baselineSolver = CarefulOptimizedSolver(6, 6, initialPieces)
    val baselineResult = baselineSolver.count
    val baselineTime = (System.nanoTime() - baselineStart) / 1000000.0
    
    println(s"Baseline: $baselineResult solutions in ${baselineTime}ms")
    
    // SAT solver
    println("\nRunning SAT Solver...")
    val satStart = System.nanoTime()
    val satSolver = SATSolver(6, 6, initialPieces)
    
    val satResult = try {
      satSolver.count
    } catch {
      case e: Exception =>
        println(s"SAT solver error: ${e.getMessage}")
        -1
    }
    val satTime = (System.nanoTime() - satStart) / 1000000.0
    
    println(s"SAT Solver: $satResult solutions in ${satTime}ms")
    
    // Analysis
    if (satResult == baselineResult && satResult > 0) {
      val speedup = baselineTime / satTime
      println(f"\n✅ CORRECTNESS: SAT solver matches baseline ($baselineResult solutions)")
      println(f"⚡ PERFORMANCE: ${speedup}%.2fx ${if (speedup > 1) "faster" else "slower"}")
      
      if (speedup > 5) {
        println("🚀 EXCELLENT: SAT shows major speedup potential!")
      } else if (speedup > 1.5) {
        println("✅ GOOD: SAT shows speedup, promising for 7x7")
      } else if (speedup > 0.5) {
        println("⚡ ACCEPTABLE: SAT competitive, may scale better")
      } else {
        println("⚠️ SLOW: SAT needs optimization")
      }
      
    } else if (satResult > 0) {
      println(f"❌ CORRECTNESS ISSUE: Results differ (Baseline: $baselineResult, SAT: $satResult)")
      println("SAT solver encoding may have bugs")
    } else {
      println("❌ SAT SOLVER FAILED: Implementation needs debugging")
      println("Common issues: CNF encoding, MiniSat integration, file I/O")
    }
  }
  
  def test7x7(initialPieces: InitialPieces): Unit = {
    println("--- 7x7 Board: Performance Challenge ---")
    println("Target: Beat 14.4s baseline with complete solution set")
    
    val satStart = System.nanoTime()
    val satSolver = SATSolver(7, 7, initialPieces)
    
    println("Running SAT Solver on 7x7...")
    val result = try {
      val solutions = satSolver.count
      val totalTime = (System.nanoTime() - satStart) / 1000000.0
      
      println(f"SAT Solver: $solutions solutions in ${totalTime}ms (${totalTime/1000}s)")
      
      // Compare with 14.4s baseline
      val baselineTime = 14400.0
      val speedup = baselineTime / totalTime
      
      println(f"\nComparison with 14.4s baseline:")
      println(f"SAT speedup: ${speedup}%.2fx ${if (speedup > 1) "faster" else "slower"}")
      
      if (totalTime < 1000) {
        println("🎉 SUB-SECOND: Revolutionary breakthrough!")
        println("🚀 SAT solver achieves sub-1s complete solution!")
      } else if (totalTime < 5000) {
        println("🎉 SUB-5S: Excellent SAT performance!")
        println("⚡ Major improvement over constraint satisfaction")
      } else if (totalTime < baselineTime) {
        println("✅ SUCCESS: SAT beats 14.4s baseline!")
        println("🔥 Boolean satisfiability proves superior")
      } else if (totalTime < baselineTime * 2) {
        println("⚡ COMPETITIVE: SAT shows promise")
        println("🔧 May benefit from encoding optimizations")
      } else {
        println("⏳ NEEDS WORK: SAT slower than baseline")
        println("Possible issues: CNF size, constraint encoding")
      }
      
      Some((solutions, totalTime))
      
    } catch {
      case e: Exception =>
        val elapsed = (System.nanoTime() - satStart) / 1000000.0
        println(f"SAT solver failed after ${elapsed}ms: ${e.getMessage}")
        println("\nCommon failure modes:")
        println("- CNF file too large for MiniSat")
        println("- Constraint encoding errors")
        println("- Memory limitations")
        println("- Process execution issues")
        None
    }
    
    // Final assessment
    result match {
      case Some((solutions, time)) =>
        println(f"\n🎯 SAT SOLVER STATUS: FUNCTIONAL")
        println(f"Solutions found: $solutions")
        if (time < 14400) {
          println("🏆 BREAKTHROUGH: SAT solver breaks the 14.4s barrier!")
          println("🔬 This validates Boolean satisfiability for chess constraints")
        }
        
      case None =>
        println(f"\n🔧 SAT SOLVER STATUS: NEEDS DEBUGGING")
        println("Potential improvements:")
        println("- Optimize CNF encoding (fewer variables/clauses)")
        println("- Try different SAT solvers (Glucose, Lingeling)")
        println("- Add incremental solving for multiple solutions")
        println("- Implement symmetry breaking in Boolean domain")
    }
  }
}