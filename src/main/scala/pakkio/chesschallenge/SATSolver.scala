package pakkio.chesschallenge

import scala.collection.mutable
import scala.sys.process._
import java.io.{File, PrintWriter}
import scala.io.Source

// SAT-based chess constraint solver using MiniSat
case class SATSolver(m: Int, n: Int, pieces: InitialPieces) {
  type Solutions = Set[Set[PieceAtSlot]]
  
  // Variable encoding: each piece at each position gets a unique variable number
  private case class Variable(piece: Piece, pieceIndex: Int, slot: Slot) {
    val id: Int = encodeVariable(piece, pieceIndex, slot)
  }
  
  private var nextVarId = 1
  private val varMap = mutable.Map[(Piece, Int, Slot), Int]()
  private val reverseVarMap = mutable.Map[Int, (Piece, Int, Slot)]()
  
  private val pieceList = flatPieces(pieces)
  
  lazy val solution: Solutions = {
    val startTime = System.nanoTime()
    println("Starting SAT-based chess solver...")
    println(s"Board size: ${m}×${n}, Pieces: ${pieceList.map(_.mnemonic).mkString}")
    
    val result = solveWithSAT()
    val totalTime = (System.nanoTime() - startTime) / 1000000.0
    
    println(f"SAT Solver completed in ${totalTime}ms")
    result
  }
  
  def count: Int = solution.size
  
  // Encode a piece at a position to a unique variable ID
  private def encodeVariable(piece: Piece, pieceIndex: Int, slot: Slot): Int = {
    val key = (piece, pieceIndex, slot)
    varMap.getOrElseUpdate(key, {
      val id = nextVarId
      nextVarId += 1
      reverseVarMap(id) = key
      id
    })
  }
  
  // Get variable ID for piece at position
  private def getVar(piece: Piece, pieceIndex: Int, slot: Slot): Int = {
    encodeVariable(piece, pieceIndex, slot)
  }
  
  // Generate all variables for the problem
  private def generateVariables(): Unit = {
    val groupedPieces = pieceList.zipWithIndex.groupBy(_._1).mapValues(_.map(_._2))
    
    for {
      (piece, indices) <- groupedPieces
      pieceIndex <- indices
      x <- 0 until m
      y <- 0 until n
      slot = Slot(x, y)
    } {
      getVar(piece, pieceIndex, slot) // This creates the variable
    }
    
    println(s"Generated ${nextVarId - 1} Boolean variables")
  }
  
  // Generate CNF clauses for constraints
  private def generateCNF(): List[List[Int]] = {
    val clauses = mutable.ListBuffer[List[Int]]()
    
    // 1. Each piece must be placed exactly once
    val groupedPieces = pieceList.zipWithIndex.groupBy(_._1).mapValues(_.map(_._2))
    
    for ((piece, indices) <- groupedPieces; pieceIndex <- indices) {
      // At least one position (positive clause)
      val atLeastOne = (for {
        x <- 0 until m
        y <- 0 until n
      } yield getVar(piece, pieceIndex, Slot(x, y))).toList
      
      if (atLeastOne.nonEmpty) {
        clauses += atLeastOne
      }
      
      // At most one position (negative pairs)
      val allPositions = for {
        x <- 0 until m
        y <- 0 until n
      } yield (x, y)
      
      for {
        i <- allPositions.indices
        j <- allPositions.indices
        if i < j // Avoid duplicates
        (x1, y1) = allPositions(i)
        (x2, y2) = allPositions(j)
      } {
        val var1 = getVar(piece, pieceIndex, Slot(x1, y1))
        val var2 = getVar(piece, pieceIndex, Slot(x2, y2))
        clauses += List(-var1, -var2) // NOT (piece at pos1 AND piece at pos2)
      }
    }
    
    // 2. No two pieces at the same position
    for {
      x <- 0 until m
      y <- 0 until n
      slot = Slot(x, y)
      allPiecesAtSlot = pieceList.zipWithIndex.map { case (piece, idx) => getVar(piece, idx, slot) }
      // For each pair of pieces, they can't both be at the same slot
      var1 <- allPiecesAtSlot
      var2 <- allPiecesAtSlot
      if var1 < var2
    } {
      clauses += List(-var1, -var2)
    }
    
    // 3. No attacking pairs
    for {
      (piece1, idx1) <- pieceList.zipWithIndex
      (piece2, idx2) <- pieceList.zipWithIndex
      if idx1 < idx2 // Avoid duplicate constraints
      slot1 <- getAllSlots()
      slot2 <- getAllSlots()
      if slot1 != slot2 && piecesAttack(piece1, slot1, piece2, slot2)
    } {
      val var1 = getVar(piece1, idx1, slot1)
      val var2 = getVar(piece2, idx2, slot2)
      clauses += List(-var1, -var2) // NOT (piece1 at slot1 AND piece2 at slot2)
    }
    
    println(s"Generated ${clauses.size} CNF clauses")
    clauses.toList
  }
  
  private def getAllSlots(): List[Slot] = {
    (for {
      x <- 0 until m
      y <- 0 until n
    } yield Slot(x, y)).toList
  }
  
  // Check if two pieces attack each other
  private def piecesAttack(piece1: Piece, slot1: Slot, piece2: Piece, slot2: Slot): Boolean = {
    // Create a temporary board to use existing attack logic
    val board = Board(m, n, Set.empty)
    val attackedByPiece1 = piece1.getAttackedSlots(board, slot1)
    attackedByPiece1.contains(slot2)
  }
  
  // Write CNF to DIMACS format
  private def writeDIMACS(clauses: List[List[Int]], filename: String): Unit = {
    val writer = new PrintWriter(new File(filename))
    try {
      writer.println(s"p cnf ${nextVarId - 1} ${clauses.size}")
      for (clause <- clauses) {
        writer.println(clause.mkString(" ") + " 0")
      }
    } finally {
      writer.close()
    }
  }
  
  // Parse SAT solver output
  private def parseSATOutput(filename: String): List[Set[PieceAtSlot]] = {
    val solutions = mutable.ListBuffer[Set[PieceAtSlot]]()
    
    try {
      val lines = Source.fromFile(filename).getLines().toList
      
      // MiniSat outputs model on lines starting with 'v'
      val modelLines = lines.filter(_.startsWith("v "))
      
      if (modelLines.nonEmpty) {
        // Combine all model lines (MiniSat may split long models)
        val fullModel = modelLines.flatMap(line => 
          line.substring(2).trim.split("\\s+").map(_.toInt).filter(_ != 0)
        )
        
        if (fullModel.nonEmpty) {
          val solution = extractSolution(fullModel.toList)
          if (solution.nonEmpty) {
            solutions += solution
          }
        }
      } else {
        // Try to read the output directly (some versions output differently)
        println("No 'v' lines found, checking output format...")
        println(s"File contents: ${lines.take(5).mkString("; ")}")
      }
    } catch {
      case e: Exception =>
        println(s"Error parsing SAT output: ${e.getMessage}")
    }
    
    solutions.toList
  }
  
  // Extract chess solution from SAT assignment
  private def extractSolution(assignment: List[Int]): Set[PieceAtSlot] = {
    val solution = mutable.Set[PieceAtSlot]()
    
    for (varId <- assignment if varId > 0) {
      reverseVarMap.get(varId) match {
        case Some((piece, pieceIndex, slot)) =>
          solution += PieceAtSlot(piece, slot)
        case None =>
          println(s"Warning: Unknown variable ID $varId")
      }
    }
    
    solution.toSet
  }
  
  // Main SAT solving logic - finds ALL solutions
  private def solveWithSAT(): Solutions = {
    generateVariables()
    val baseClauses = generateCNF()
    
    val solutions = mutable.Set[Set[PieceAtSlot]]()
    var additionalClauses = List.empty[List[Int]]
    var iteration = 0
    
    while (iteration < 5000) { // Safety limit
      val dimacsFile = s"chess_problem_$iteration.cnf"
      val outputFile = s"chess_solution_$iteration.out"
      
      // Combine base constraints with blocking clauses
      val allClauses = baseClauses ++ additionalClauses
      writeDIMACS(allClauses, dimacsFile)
      
      try {
        val cmd = s"minisat $dimacsFile $outputFile"
        val exitCode = cmd.!
        
        if (iteration % 100 == 0 && iteration > 0) {
          println(s"SAT iteration $iteration: ${solutions.size} solutions found so far")
        }
        
        if (exitCode == 10) { // SATISFIABLE
          val resultFile = new File(outputFile)
          if (resultFile.exists()) {
            val content = Source.fromFile(resultFile).getLines().toList
            
            if (content.nonEmpty && content.head == "SAT" && content.length > 1) {
              val modelString = content(1)
              val assignment = modelString.split("\\s+").map(_.toInt).filter(_ != 0).toList
              val solution = extractSolution(assignment)
              
              if (solution.nonEmpty && !solutions.contains(solution)) {
                solutions += solution
                
                // Create blocking clause to exclude this solution
                val blockingClause = createBlockingClause(assignment)
                additionalClauses = blockingClause :: additionalClauses
                
                // Clean up this iteration's files
                new File(dimacsFile).delete()
                new File(outputFile).delete()
              } else {
                // This shouldn't happen, but if it does, we're done
                new File(dimacsFile).delete()
                new File(outputFile).delete()
                println("Warning: Solution extraction failed or duplicate found")
              }
            }
          }
        } else if (exitCode == 20) { // UNSATISFIABLE
          // No more solutions
          new File(dimacsFile).delete()
          new File(outputFile).delete()
          println(s"SAT enumeration complete: found ${solutions.size} total solutions")
          return solutions.toSet
        } else {
          // Error
          new File(dimacsFile).delete()
          new File(outputFile).delete()
          println(s"SAT solver error (exit code: $exitCode) at iteration $iteration")
          return solutions.toSet
        }
      } catch {
        case e: Exception =>
          println(s"Error in SAT iteration $iteration: ${e.getMessage}")
          try {
            new File(dimacsFile).delete()
            new File(outputFile).delete()
          } catch {
            case _: Exception => // Ignore cleanup errors
          }
          return solutions.toSet
      }
      
      iteration += 1
    }
    
    println(s"SAT enumeration stopped at iteration limit: ${solutions.size} solutions found")
    solutions.toSet
  }
  
  // Create a blocking clause to exclude a specific solution
  private def createBlockingClause(assignment: List[Int]): List[Int] = {
    // A blocking clause contains the negation of all positive assignments
    // This ensures this exact solution cannot occur again
    assignment.map(-_)  // Negate all variables in the solution
  }
  
  private def flatPieces(pieces: InitialPieces): List[Piece] = {
    val pieceList = for {
      (p, n) <- pieces.list
      _ <- 1 to n
    } yield p
    pieceList.toList
  }
}