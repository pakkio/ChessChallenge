package pakkio.chesschallenge

// Concrete example showing 8-way symmetry with all pieces
object SymmetryExample {
  
  def main(args: Array[String]): Unit = {
    println("=== Complete Solution Symmetry Example ===")
    
    val pieces = InitialPieces(Map(King -> 2, Queen -> 2, Bishop -> 2, Knight -> 1))
    
    // Get one specific solution to demonstrate
    val solver = HighlyOptimizedParallelSolution(6, 6, pieces)
    val solutions = solver.solution
    
    // Take the first solution as our example
    val exampleSolution = solutions.head
    
    println("Original Solution:")
    printSolution(exampleSolution, 6, 6)
    
    println("\n=== 8 Symmetric Variants ===")
    
    // Generate all 8 symmetric variants
    val variants = List(
      ("Original (Identity)", exampleSolution),
      ("90° Rotation", exampleSolution.map(rotate90(_, 6, 6))),
      ("180° Rotation", exampleSolution.map(rotate180(_, 6, 6))),
      ("270° Rotation", exampleSolution.map(rotate270(_, 6, 6))),
      ("Horizontal Reflection", exampleSolution.map(reflectX(_, 6, 6))),
      ("Vertical Reflection", exampleSolution.map(reflectY(_, 6, 6))),
      ("H.Reflect + 90°", exampleSolution.map(reflectXThenRotate90(_, 6, 6))),
      ("V.Reflect + 90°", exampleSolution.map(reflectYThenRotate90(_, 6, 6)))
    )
    
    variants.foreach { case (name, variant) =>
      println(s"\n$name:")
      printSolution(variant, 6, 6)
    }
    
    // Verify all variants are valid solutions
    println("\n=== Validation ===")
    variants.zipWithIndex.foreach { case ((name, variant), idx) =>
      val board = Board(6, 6, variant)
      val isValid = board.isSafe
      println(s"${idx + 1}. $name: ${if (isValid) "✅ VALID" else "❌ INVALID"}")
    }
    
    println(s"\nAll 8 variants represent the SAME pattern, but the original algorithm")
    println(s"counted them as separate solutions. Symmetry elimination keeps only")
    println(s"1 canonical form and reconstructs the other 7 when needed.")
  }
  
  private def printSolution(solution: Set[PieceAtSlot], m: Int, n: Int): Unit = {
    val board = Array.fill(n, m)("-")
    
    solution.foreach { pieceAtSlot =>
      val Slot(x, y) = pieceAtSlot.slot
      if (x >= 0 && x < m && y >= 0 && y < n) {
        board(n - 1 - y)(x) = pieceAtSlot.piece.mnemonic
      }
    }
    
    board.foreach { row =>
      println("  " + row.mkString(" "))
    }
    
    // Also show piece coordinates
    val sortedPieces = solution.toList.sortBy(p => (p.piece.toString, p.slot.x, p.slot.y))
    println("  Pieces: " + sortedPieces.map(p => s"${p.piece.mnemonic}(${p.slot.x},${p.slot.y})").mkString(" "))
  }
  
  // Transform functions
  private def rotate90(pieceAtSlot: PieceAtSlot, m: Int, n: Int): PieceAtSlot = {
    val Slot(x, y) = pieceAtSlot.slot
    PieceAtSlot(pieceAtSlot.piece, Slot(n - 1 - y, x))
  }
  
  private def rotate180(pieceAtSlot: PieceAtSlot, m: Int, n: Int): PieceAtSlot = {
    val Slot(x, y) = pieceAtSlot.slot
    PieceAtSlot(pieceAtSlot.piece, Slot(m - 1 - x, n - 1 - y))
  }
  
  private def rotate270(pieceAtSlot: PieceAtSlot, m: Int, n: Int): PieceAtSlot = {
    val Slot(x, y) = pieceAtSlot.slot
    PieceAtSlot(pieceAtSlot.piece, Slot(y, m - 1 - x))
  }
  
  private def reflectX(pieceAtSlot: PieceAtSlot, m: Int, n: Int): PieceAtSlot = {
    val Slot(x, y) = pieceAtSlot.slot
    PieceAtSlot(pieceAtSlot.piece, Slot(m - 1 - x, y))
  }
  
  private def reflectY(pieceAtSlot: PieceAtSlot, m: Int, n: Int): PieceAtSlot = {
    val Slot(x, y) = pieceAtSlot.slot
    PieceAtSlot(pieceAtSlot.piece, Slot(x, n - 1 - y))
  }
  
  private def reflectXThenRotate90(pieceAtSlot: PieceAtSlot, m: Int, n: Int): PieceAtSlot = {
    rotate90(reflectX(pieceAtSlot, m, n), m, n)
  }
  
  private def reflectYThenRotate90(pieceAtSlot: PieceAtSlot, m: Int, n: Int): PieceAtSlot = {
    rotate90(reflectY(pieceAtSlot, m, n), m, n)
  }
}