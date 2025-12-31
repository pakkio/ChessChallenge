package pakkio.chesschallenge

import java.util.concurrent.ConcurrentHashMap
import scala.collection.mutable

// Ultra-optimized chess solver - v6 (Bitboard)
// Key Strategy:
// 1. Bitboards: Represent the 6x6 board using a single Long (64 bits). 
//    Set operations become bitwise AND/OR/NOT (CPU native).
// 2. Early Pruning: Check symmetry at every step.
// 3. Flattened Recursion: Minimal object creation.
case class UltraOptimizedSolution(m: Int, n: Int, pieces: InitialPieces) {
  
  // Requirement: m * n <= 64 for Long bitboard
  require(m * n <= 64, "Board too large for bitboard optimization")
  
  type Solutions = Set[Set[PieceAtSlot]]
  
  // Cache for seen canonical forms to prune branches
  private val seenCanonical = new ConcurrentHashMap[Long, Boolean]()
  
  // --- Precomputations ---
  
  // Map Piece -> Int ID for array indexing
  private val pieceTypeId: Map[String, Int] = Map(
    "K" -> 0, "Q" -> 1, "R" -> 2, "B" -> 3, "N" -> 4
  )
  
  // Attack masks: [PieceID][SquareIndex] -> AttackBitMask
  // 5 pieces * 64 squares = 320 longs (tiny)
  private val attackMasks: Array[Array[Long]] = {
    val masks = Array.ofDim[Long](5, m * n)
    val boundsBoard = Board(m, n, Set()) // Temp board for logic reuse
    
    val pTypes = List(King, Queen, Rook, Bishop, Knight)
    for (p <- pTypes) {
      val pid = pieceTypeId(p.mnemonic)
      for (i <- 0 until m * n) {
        val x = i / n
        val y = i % n
        val slot = Slot(x, y)
        val attacked = p.getAttackedSlots(boundsBoard, slot)
        var mask = 0L
        for (s <- attacked) {
          mask |= 1L << (s.x * n + s.y)
        }
        masks(pid)(i) = mask
      }
    }
    masks
  }
  
  // Precomputed transformation maps for symmetry: [TransformID][SquareIndex] -> TransformedSquareIndex
  // 8 transforms * 64 squares
  private val symmetryMaps: Array[Array[Int]] = {
    val maps = Array.ofDim[Int](8, m * n)
    
    // Transform functions (x, y) -> (nx, ny)
    val transforms = Array[(Int, Int) => (Int, Int)](
      (x, y) => (x, y),                   // Identity
      (x, y) => (n - 1 - y, x),           // 90
      (x, y) => (m - 1 - x, n - 1 - y),   // 180
      (x, y) => (y, m - 1 - x),           // 270
      (x, y) => (m - 1 - x, y),           // RefX
      (x, y) => (x, n - 1 - y),           // RefY
      (x, y) => (n - 1 - y, m - 1 - x),   // RefX + 90
      (x, y) => (y, x)                    // RefY + 90
    )
    
    for (t <- 0 until 8) {
      for (i <- 0 until m * n) {
        val x = i / n
        val y = i % n
        val (nx, ny) = transforms(t)(x, y)
        maps(t)(i) = nx * n + ny
      }
    }
    maps
  }
  
  // Sorted flattened piece list
  private val pieceListWithIds: List[(Piece, Int)] = {
    def attackPower(p: Piece): Int = p match {
      case Queen  => 5
      case Rook   => 4  
      case Bishop => 3
      case Knight => 2
      case King   => 1
      case _      => 0
    }
    flatPieces(pieces)
      .sortBy(p => -attackPower(p))
      .map(p => (p, pieceTypeId(p.mnemonic)))
  }
  
  private def flatPieces(pieces: InitialPieces): List[Piece] = {
    pieces.list.flatMap { case (p, n) => List.fill(n)(p) }.toList
  }
  
  // --- Core Logic ---
  
  lazy val solution: Solutions = {
    val results = solve(pieceListWithIds, 0L, 0L, List.empty)
    // Convert back to heavy Board objects only at the very end
    results.map(_.toSet).toSet
  }
  
  def count: Int = solution.size

  // Returns List of solutions, where each solution is a List of PieceAtSlot
  private def solve(
    remaining: List[(Piece, Int)], 
    occupied: Long, 
    unsafe: Long, 
    currentPlacements: List[PieceAtSlot]
  ): List[List[PieceAtSlot]] = {
    
    remaining match {
      case Nil => 
        List(currentPlacements)

      case (piece, pid) :: rest =>
        val results = mutable.ListBuffer[List[PieceAtSlot]]()
        
        // Parallelize at top level for speed
        // If we are deep in recursion, stay sequential to avoid overhead
        val isTopLevel = currentPlacements.size < 2 
        
        // Find all safely available bits
        // available = ~(unsafe | occupied) & valid_board_mask
        // But since we iterate 0..m*n, we just check if bit is set in (unsafe | occupied)
        
        var i = 0
        val len = m * n
        
        // Collect valid moves first to potentially parallelize
        val validMoves = new mutable.ArrayBuffer[Int](len)
        while (i < len) {
           // check if bit i is NOT set in (unsafe | occupied)
           if (((unsafe | occupied) & (1L << i)) == 0) {
             // Safe square. Now check if placing here attacks existing pieces
             val attacks = attackMasks(pid)(i)
             if ((attacks & occupied) == 0) {
              validMoves += i
             }
           }
           i += 1
        }
        
        // Process moves
        def processMove(idx: Int): List[List[PieceAtSlot]] = {
          val x = idx / n
          val y = idx % n
          val slot = Slot(x, y)
          val pas = PieceAtSlot(piece, slot)
          
          val newPlacements = pas :: currentPlacements
          
          // Check Canonical Form
          // We need to hash the minimal canonical representation of newPlacements
          val hash = computeCanonicalHash(newPlacements)
          
          if (seenCanonical.putIfAbsent(hash, true) == null) {
            // New valid unique state found. Recurse.
            val newOccupied = occupied | (1L << idx)
            val newUnsafe = unsafe | attackMasks(pid)(idx)
            solve(rest, newOccupied, newUnsafe, newPlacements)
          } else {
            Nil
          }
        }
        
        if (isTopLevel && validMoves.size > 0) {
          // Parallel execution for top level
           validMoves.par.flatMap(processMove).toList
        } else {
          // Sequential
          validMoves.flatMap(processMove).toList
        }
    }
  }
  
  
  // Hashing optimization: 
  // We need a stable hash for the simplified state (List[PieceAtSlot])
  private def computeCanonicalHash(placements: List[PieceAtSlot]): Long = {
    // 1. Convert placements to an array of (PieceID, SquareIndex)
    val data = new Array[Int](placements.size) // Encoded as (Pid << 8) | Idx
    var k = 0
    var iter = placements
    while (iter.nonEmpty) {
      val p = iter.head
      val pid = pieceTypeId(p.piece.mnemonic)
      val idx = p.slot.x * n + p.slot.y
      // Use 16 bits for PID to allow safe shifting later
      data(k) = (pid << 16) | idx
      iter = iter.tail
      k += 1
    }
    
    // 2. Apply all 8 symmetries and find the minimum hash
    var minH = Long.MaxValue
    
    var t = 0
    while (t < 8) {
      val codes = new Array[Long](k)
      var j = 0
      while (j < k) {
        val raw = data(j)
        val pid = raw >>> 16
        val oldIdx = raw & 0xFFFF
        val newIdx = symmetryMaps(t)(oldIdx)
        // Store as (newIdx << 16) | pid. Sorting by this Long sorts by Index then PID
        codes(j) = (newIdx.toLong << 16) | pid
        j += 1
      }
      java.util.Arrays.sort(codes)
      
      // Improved FNV-1a inspired hash with better mixing
      var h = -3750763034362895579L // Offset basis
      j = 0
      while (j < k) {
        val code = codes(j)
        h = (h ^ code) * 1099511628211L // FNV prime
        j += 1
      }
      // Final avalanche
      h = h ^ (h >>> 32)
      
      if (h < minH) minH = h
      t += 1
    }
    minH
  }

  def printSolutions(): Unit = {
    solution.foreach(d => Board(m, n, d).printBoard(""))
  }
}
