package pakkio.chesschallenge

object Main extends App {
  println("Starting the Parallel Chess Challenge!")
  println("Placing 2 Kings, 2 Queens, 2 Bishops, and 1 Knight on a 6x6 board...")
  
  val startTime = System.currentTimeMillis()
  
  val initialPieces = InitialPieces(Map(King -> 2, Queen -> 2, Bishop -> 2, Knight -> 1))
  val solution = ParallelSolution(6, 6, initialPieces)
  
  val endTime = System.currentTimeMillis()
  val duration = (endTime - startTime) / 1000.0
  
  println(s"Found ${solution.count} solutions in ${duration} seconds")
}