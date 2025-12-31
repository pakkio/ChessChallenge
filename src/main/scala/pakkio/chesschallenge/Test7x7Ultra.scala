package pakkio.chesschallenge

object Test7x7Ultra extends App {
  val initialPieces = InitialPieces(Map(King -> 2, Queen -> 2, Bishop -> 2, Knight -> 1))
  
  println("=" * 60)
  println("7x7 BOARD ULTRA BENCHMARK")
  println("=" * 60)
  
  val start = System.nanoTime()
  val solution = UltraOptimizedSolution(7, 7, initialPieces)
  val result = solution.count
  val end = System.nanoTime()
  
  val duration = (end - start) / 1000000.0
  println(s"Solutions found: $result")
  println(s"Time: ${duration.formatted("%.1f")}ms (${(duration/1000).formatted("%.2f")}s)")
  println("=" * 60)
}
