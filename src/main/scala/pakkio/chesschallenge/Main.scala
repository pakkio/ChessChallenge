package pakkio.chesschallenge

object Main extends App {
  println("Starting the Highly Optimized Parallel Chess Challenge!")
  println("Placing 2 Kings, 2 Queens, 2 Bishops, and 1 Knight on a 6x6 board...")
  println(s"Running on ${Runtime.getRuntime.availableProcessors} CPU cores")
  
  // Warmup JVM first
  println("Warming up JVM...")
  val initialPieces = InitialPieces(Map(King -> 2, Queen -> 2, Bishop -> 2, Knight -> 1))
  val warmupSolution = HighlyOptimizedParallelSolution(6, 6, initialPieces)
  warmupSolution.count // Force computation
  
  // Now measure actual performance after warmup
  println("Measuring actual performance...")
  val startTime = System.nanoTime()
  
  val solution = HighlyOptimizedParallelSolution(6, 6, initialPieces)
  val result = solution.count
  
  val endTime = System.nanoTime()
  val durationMs = (endTime - startTime) / 1000000.0
  
  println(s"Found $result solutions in ${durationMs}ms")
  println(s"Performance: ${(6000.0/durationMs).formatted("%.1f")}x faster than previous 6-second version")
}