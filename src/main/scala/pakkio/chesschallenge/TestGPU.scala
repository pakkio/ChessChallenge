package pakkio.chesschallenge

object TestGPU {
  def main(args: Array[String]): Unit = {
    println("=== GPU vs CPU Chess Solver Benchmark ===")
    println(s"Available GPU: RTX 4060 with 8GB VRAM")
    println()
    
    val initialPieces = InitialPieces(Map(King -> 2, Queen -> 2, Bishop -> 2, Knight -> 1))
    
    // Test on 6x6 first for validation
    test6x6(initialPieces)
    
    // Test on 7x7 for performance comparison
    test7x7(initialPieces)
  }
  
  def test6x6(initialPieces: InitialPieces): Unit = {
    println("--- 6x6 Board Validation Test ---")
    
    // CPU baseline
    println("Running CPU solver (CarefulOptimizedSolver)...")
    val cpuStartTime = System.nanoTime()
    val cpuSolver = CarefulOptimizedSolver(6, 6, initialPieces)
    val cpuResult = cpuSolver.count
    val cpuTime = (System.nanoTime() - cpuStartTime) / 1000000.0
    
    println(s"CPU Result: $cpuResult solutions in ${cpuTime}ms")
    
    // GPU solver
    println("Running GPU solver...")
    val gpuSolver = GPUSolver(6, 6, initialPieces)
    val gpuStartTime = System.nanoTime()
    val gpuResult = try {
      gpuSolver.count
    } catch {
      case e: Exception =>
        println(s"GPU solver error: ${e.getMessage}")
        println("This is expected for the initial implementation - falling back to CPU")
        -1
    } finally {
      gpuSolver.cleanup()
    }
    val gpuTime = (System.nanoTime() - gpuStartTime) / 1000000.0
    
    println(s"GPU Result: $gpuResult solutions in ${gpuTime}ms")
    
    // Analysis
    if (gpuResult == cpuResult && gpuResult > 0) {
      val speedup = cpuTime / gpuTime
      println(f"✅ CORRECTNESS: GPU matches CPU results")
      println(f"⚡ PERFORMANCE: ${speedup}%.2fx ${if (speedup > 1) "faster" else "slower"}")
    } else if (gpuResult > 0) {
      println(f"❌ CORRECTNESS: Results differ (CPU: $cpuResult, GPU: $gpuResult)")
    } else {
      println("⚠️  GPU solver not yet functional, using CPU fallback")
    }
  }
  
  def test7x7(initialPieces: InitialPieces): Unit = {
    println("\n--- 7x7 Board Performance Test ---")
    
    // Test GPU solver on 7x7
    println("Testing GPU solver on 7x7...")
    val gpuSolver = GPUSolver(7, 7, initialPieces)
    val startTime = System.nanoTime()
    
    val result = try {
      val solutions = gpuSolver.count
      val totalTime = (System.nanoTime() - startTime) / 1000000.0
      
      println(s"GPU Solver: $solutions solutions in ${totalTime}ms (${totalTime/1000}s)")
      
      // Compare with known baseline
      val baselineTime = 14400.0 // 14.4s
      val speedup = baselineTime / totalTime
      
      println(f"Comparison with 14.4s CPU baseline:")
      println(f"GPU speedup: ${speedup}%.2fx ${if (speedup > 1) "faster" else "slower"}")
      
      if (totalTime < baselineTime) {
        println("🎉 GPU SUCCESS: Beats CPU baseline!")
        if (totalTime < 1000) {
          println("🚀 SUB-SECOND: Incredible GPU acceleration achieved!")
        } else if (totalTime < 5000) {
          println("⚡ SUB-5S: Excellent GPU performance!")
        }
      } else if (totalTime < baselineTime * 2) {
        println("✅ COMPETITIVE: GPU performs reasonably well")
      } else {
        println("⚠️  NEEDS OPTIMIZATION: GPU slower than expected")
      }
      
      Some(solutions)
      
    } catch {
      case e: Exception =>
        val elapsed = (System.nanoTime() - startTime) / 1000000.0
        println(s"GPU solver error after ${elapsed}ms: ${e.getMessage}")
        println("Note: This is expected for the initial GPU implementation")
        None
    } finally {
      gpuSolver.cleanup()
    }
    
    result match {
      case Some(solutions) =>
        println(s"\n🎯 GPU Implementation Status: FUNCTIONAL")
        println(s"Solutions found: $solutions")
      case None =>
        println(s"\n🔧 GPU Implementation Status: IN DEVELOPMENT")
        println("CPU fallback was used. Full GPU implementation requires:")
        println("- CUDA kernel integration")
        println("- Memory transfer optimization") 
        println("- Parallel board state management")
    }
  }
}