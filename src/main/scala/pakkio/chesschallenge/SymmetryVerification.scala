package pakkio.chesschallenge

// Verification utility to prove symmetry elimination correctness
object SymmetryVerification {
  
  def main(args: Array[String]): Unit = {
    println("=== Symmetry Elimination Verification ===")
    
    val pieces = InitialPieces(Map(King -> 2, Queen -> 2, Bishop -> 2, Knight -> 1))
    
    // Get original solutions (without symmetry elimination)
    println("Computing original solutions (no symmetry elimination)...")
    val originalSolver = OriginalSolution(6, 6, pieces)
    val originalSolutions = originalSolver.solution
    println(s"Original solutions: ${originalSolutions.size}")
    
    // Get solutions with symmetry elimination
    println("Computing solutions with symmetry elimination...")
    val symmetrySolver = HighlyOptimizedParallelSolution(6, 6, pieces)
    val symmetrySolutions = symmetrySolver.solution
    println(s"Symmetry-eliminated solutions: ${symmetrySolutions.size}")
    
    // Test: expand each symmetry solution to its 8 variants
    println("Expanding symmetric solutions to verify completeness...")
    val expandedSolutions = symmetrySolutions.flatMap(expandToAllSymmetries(_, 6, 6))
    println(s"Expanded solutions: ${expandedSolutions.size}")
    
    // Verification
    println("\n=== VERIFICATION RESULTS ===")
    println(s"Original count: ${originalSolutions.size}")
    println(s"Symmetry count: ${symmetrySolutions.size}")
    println(s"Expanded count: ${expandedSolutions.size}")
    println(s"Expected ratio: ${originalSolutions.size.toDouble / symmetrySolutions.size}")
    
    // Check if expanded set matches original set
    val originalSet = originalSolutions.map(normalizeForComparison)
    val expandedSet = expandedSolutions.map(normalizeForComparison)
    
    val missing = originalSet -- expandedSet
    val extra = expandedSet -- originalSet
    
    println(s"Missing solutions: ${missing.size}")
    println(s"Extra solutions: ${extra.size}")
    
    if (missing.isEmpty && extra.isEmpty && expandedSolutions.size == originalSolutions.size) {
      println("✅ VERIFICATION PASSED: Symmetry elimination is correct!")
    } else {
      println("❌ VERIFICATION FAILED: Symmetry elimination has issues!")
    }
  }
  
  // Expand one solution to all 8 symmetric variants
  private def expandToAllSymmetries(solution: Set[PieceAtSlot], m: Int, n: Int): Set[Set[PieceAtSlot]] = {
    Set(
      solution, // identity
      solution.map(rotate90(_, m, n)),
      solution.map(rotate180(_, m, n)),
      solution.map(rotate270(_, m, n)),
      solution.map(reflectX(_, m, n)),
      solution.map(reflectY(_, m, n)),
      solution.map(reflectXThenRotate90(_, m, n)),
      solution.map(reflectYThenRotate90(_, m, n))
    )
  }
  
  // Transform functions (same as in HighlyOptimizedParallelSolution)
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
  
  // Normalize solution for comparison (sort pieces consistently)
  private def normalizeForComparison(solution: Set[PieceAtSlot]): String = {
    solution.toList
      .sortBy(p => (p.slot.x, p.slot.y, p.piece.toString))
      .map(p => s"${p.piece.mnemonic}${p.slot.x}${p.slot.y}")
      .mkString(",")
  }
}

// Original solver without symmetry elimination for comparison
case class OriginalSolution(m: Int, n: Int, pieces: InitialPieces) {
  type Solutions = Set[Set[PieceAtSlot]]

  private val pieceList = pieces.list.toList.flatMap { case (piece, count) =>
    List.fill(count)(piece)
  }

  lazy val solution: Solutions = placePieces(pieceList, m, n)

  private def placePieces(l: List[Piece], m: Int, n: Int): Solutions = {
    l match {
      case List() => Set(Set())
      case piece :: rest =>
        val dispositions = placePieces(rest, m, n)
        val results = for {
          disposition <- dispositions
          slot <- getAllValidSlots(piece, disposition, m, n)
        } yield disposition + PieceAtSlot(piece, slot)
        results
    }
  }

  private def getAllValidSlots(piece: Piece, disposition: Set[PieceAtSlot], m: Int, n: Int): Set[Slot] = {
    val board = Board(m, n, disposition)
    board.availableSlots.filter { slot =>
      val newPieceAtSlot = PieceAtSlot(piece, slot)
      val attackedSlots = piece.getAttackedSlots(board, slot)
      val occupiedSlots = disposition.map(_.slot)
      val attacksExistingPiece = (attackedSlots & occupiedSlots).nonEmpty
      
      if (!attacksExistingPiece) {
        val isAttackedByExisting = disposition.exists { existingPiece =>
          val existingAttacks = existingPiece.piece.getAttackedSlots(board, existingPiece.slot)
          existingAttacks.contains(slot)
        }
        !isAttackedByExisting
      } else {
        false
      }
    }
  }
}