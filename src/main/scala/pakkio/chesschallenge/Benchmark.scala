package pakkio.chesschallenge

import java.io.PrintWriter

object Benchmark extends App {
  val pw = new PrintWriter("/home/pakkio/my/ChessChallenge/benchmark_results.txt")
  def log(s: String) = { println(s); pw.println(s); pw.flush() }
  
  log("=" * 60)
  log("CHESS CHALLENGE BENCHMARK")
  log("=" * 60)
  
  val initialPieces = InitialPieces(Map(King -> 2, Queen -> 2, Bishop -> 2, Knight -> 1))
  
  // Warmup
  log("\n[Warmup] Running both versions once...")
  val warmupOpt = HighlyOptimizedParallelSolution(6, 6, initialPieces)
  warmupOpt.count
  Board.count = 0
  
  val warmupOrig = Solution(6, 6, initialPieces)
  warmupOrig.count
  Board.count = 0
  
  log("[Warmup] Complete\n")
  
  // Benchmark Optimized Version
  log("-" * 60)
  log("OPTIMIZED VERSION (HighlyOptimizedParallelSolution)")
  log("-" * 60)
  
  Board.count = 0
  val startOpt = System.nanoTime()
  val solutionOpt = HighlyOptimizedParallelSolution(6, 6, initialPieces)
  val resultOpt = solutionOpt.count
  val endOpt = System.nanoTime()
  val durationOpt = (endOpt - startOpt) / 1000000.0
  val boardsOpt = Board.count
  
  log(s"Solutions found: $resultOpt")
  log(s"Time: ${durationOpt.formatted("%.1f")}ms")
  log(s"Board objects created: $boardsOpt")
  
  // Benchmark Ultra Optimized Version
  log("\n" + "-" * 60)
  log("ULTRA OPTIMIZED VERSION (UltraOptimizedSolution)")
  log("-" * 60)
  
  Board.count = 0
  val startUltra = System.nanoTime()
  val solutionUltra = UltraOptimizedSolution(6, 6, initialPieces)
  val resultUltra = solutionUltra.count
  val endUltra = System.nanoTime()
  val durationUltra = (endUltra - startUltra) / 1000000.0
  val boardsUltra = Board.count
  
  log(s"Solutions found: $resultUltra")
  log(s"Time: ${durationUltra.formatted("%.1f")}ms")
  log(s"Board objects created: $boardsUltra")
  
  // Benchmark Original Version
  log("\n" + "-" * 60)
  log("ORIGINAL VERSION (Solution)")
  log("-" * 60)
  
  Board.count = 0
  val startOrig = System.nanoTime()
  val solutionOrig = Solution(6, 6, initialPieces)
  val resultOrig = solutionOrig.count
  val endOrig = System.nanoTime()
  val durationOrig = (endOrig - startOrig) / 1000000.0
  val boardsOrig = Board.count
  
  log(s"Solutions found: $resultOrig")
  log(s"Time: ${durationOrig.formatted("%.1f")}ms")
  log(s"Board objects created: $boardsOrig")
  
  // Summary
  log("\n" + "=" * 60)
  log("SUMMARY")
  log("=" * 60)
  log(f"Original:  ${durationOrig}%.1fms | Solutions: $resultOrig")
  log(f"Optimized: ${durationOpt}%.1fms | Solutions: $resultOpt | ${durationOrig / durationOpt}%.1fx faster")
  log(f"Ultra:     ${durationUltra}%.1fms | Solutions: $resultUltra | ${durationOrig / durationUltra}%.1fx faster")
  log("=" * 60)
  pw.close()
}
