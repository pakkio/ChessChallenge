package pakkio.chesschallenge

import scala.collection.mutable.ListBuffer
import java.io.PrintWriter
import java.io.File

// Training data point for ML model
case class PlacementExample(
  // Board features
  boardSize: Int,
  occupiedSquares: Int,
  remainingPieces: Int,
  
  // Piece being placed
  pieceType: String,
  candidateX: Int,
  candidateY: Int,
  
  // Board state features
  attackedSquares: Int,
  safeSquares: Int,
  cornerDistance: Double,
  centerDistance: Double,
  
  // Piece interaction features
  nearbyPieces: Int,
  threatenedByExisting: Boolean,
  threatensExisting: Boolean,
  
  // Target: does this placement lead to valid solutions?
  successful: Boolean
)

// Enhanced solver that collects training data during search
case class TrainingDataCollector(m: Int, n: Int, pieces: InitialPieces) {
  private val trainingData = ListBuffer[PlacementExample]()
  private val pieceList = flatPieces(pieces)
  
  def collectTrainingData(): List[PlacementExample] = {
    trainingData.clear()
    val solutions = placePiecesWithDataCollection(pieceList, m, n)
    println(s"Collected ${trainingData.size} training examples from ${solutions.size} solutions")
    trainingData.toList
  }
  
  private def flatPieces(pieces: InitialPieces): List[Piece] = {
    val pieceList = for {
      (p, n) <- pieces.list
      _ <- 1 to n
    } yield p
    pieceList.toList
  }
  
  private def placePiecesWithDataCollection(l: List[Piece], m: Int, n: Int): Set[Set[PieceAtSlot]] = {
    l match {
      case List() => Set(Set())
      case piece :: rest =>
        val dispositions = placePiecesWithDataCollection(rest, m, n)
        
        val results = scala.collection.mutable.Set[Set[PieceAtSlot]]()
        
        for (disposition <- dispositions) {
          val validPlacements = getValidPlacementsWithData(piece, disposition, m, n)
          results ++= validPlacements
        }
        
        results.toSet
    }
  }
  
  private def getValidPlacementsWithData(piece: Piece, disposition: Set[PieceAtSlot], m: Int, n: Int): Set[Set[PieceAtSlot]] = {
    val baseBoard = Board(m, n, disposition)
    val availableSlots = baseBoard.availableSlots
    val occupiedSlots = disposition.map(_.slot)
    
    val results = scala.collection.mutable.Set[Set[PieceAtSlot]]()
    
    // Try all positions (available + some occupied for negative examples)
    val allSlots = (0 until m).flatMap(x => (0 until n).map(y => Slot(x, y)))
    
    for (slot <- allSlots) {
      val newPieceAtSlot = PieceAtSlot(piece, slot)
      
      // Extract features for this placement attempt
      val features = extractFeatures(piece, slot, disposition, baseBoard, m, n)
      
      // Determine if this placement is valid/successful
      val isAvailable = availableSlots.contains(slot)
      val attackedSlots = piece.getAttackedSlots(baseBoard, slot)
      val attacksExistingPiece = (attackedSlots & occupiedSlots).nonEmpty
      
      val isAttackedByExisting = disposition.exists { existingPiece =>
        val existingAttacks = existingPiece.piece.getAttackedSlots(baseBoard, existingPiece.slot)
        existingAttacks.contains(slot)
      }
      
      val isValidPlacement = isAvailable && !attacksExistingPiece && !isAttackedByExisting
      
      // Record training example
      trainingData += PlacementExample(
        boardSize = m * n,
        occupiedSquares = disposition.size,
        remainingPieces = pieceList.length - disposition.size - 1,
        pieceType = piece.mnemonic,
        candidateX = slot.x,
        candidateY = slot.y,
        attackedSquares = features._1,
        safeSquares = features._2,
        cornerDistance = features._3,
        centerDistance = features._4,
        nearbyPieces = features._5,
        threatenedByExisting = isAttackedByExisting,
        threatensExisting = attacksExistingPiece,
        successful = isValidPlacement
      )
      
      // Add valid placements to results
      if (isValidPlacement) {
        val newDisposition = disposition + newPieceAtSlot
        results += newDisposition
      }
    }
    
    results.toSet
  }
  
  // Extract numerical features for ML model
  private def extractFeatures(piece: Piece, slot: Slot, disposition: Set[PieceAtSlot], 
                             board: Board, m: Int, n: Int): (Int, Int, Double, Double, Int) = {
    // Attack pattern analysis
    val attackedSlots = piece.getAttackedSlots(board, slot)
    val attackedCount = attackedSlots.size
    val safeSquares = (m * n) - disposition.size - attackedCount
    
    // Position analysis
    val cornerDistance = math.min(
      math.min(slot.x, m - 1 - slot.x) + math.min(slot.y, n - 1 - slot.y),
      math.min(slot.x, m - 1 - slot.x) + math.min(slot.y, n - 1 - slot.y)
    ).toDouble
    
    val centerX = (m - 1) / 2.0
    val centerY = (n - 1) / 2.0
    val centerDistance = math.sqrt(math.pow(slot.x - centerX, 2) + math.pow(slot.y - centerY, 2))
    
    // Nearby pieces analysis
    val nearbyPieces = disposition.count { pieceAtSlot =>
      val dx = math.abs(pieceAtSlot.slot.x - slot.x)
      val dy = math.abs(pieceAtSlot.slot.y - slot.y)
      dx <= 2 && dy <= 2 && !(dx == 0 && dy == 0)
    }
    
    (attackedCount, safeSquares, cornerDistance, centerDistance, nearbyPieces)
  }
  
  def saveTrainingData(filename: String): Unit = {
    val data = collectTrainingData()
    val writer = new PrintWriter(new File(filename))
    
    // CSV header
    writer.println("boardSize,occupiedSquares,remainingPieces,pieceType,candidateX,candidateY,attackedSquares,safeSquares,cornerDistance,centerDistance,nearbyPieces,threatenedByExisting,threatensExisting,successful")
    
    // Data rows
    data.foreach { example =>
      writer.println(s"${example.boardSize},${example.occupiedSquares},${example.remainingPieces},${example.pieceType},${example.candidateX},${example.candidateY},${example.attackedSquares},${example.safeSquares},${example.cornerDistance},${example.centerDistance},${example.nearbyPieces},${example.threatenedByExisting},${example.threatensExisting},${example.successful}")
    }
    
    writer.close()
    println(s"Saved ${data.size} training examples to $filename")
  }
}

// Main application to generate training data from multiple board sizes
object TrainingDataGenerator extends App {
  println("Generating training data from multiple board sizes...")
  
  val allTrainingData = ListBuffer[PlacementExample]()
  
  // Collect data from 4x4, 5x5, and 6x6 boards
  val boardConfigs = List(
    (4, 4, InitialPieces(Map(King -> 1, Queen -> 1, Bishop -> 1))),
    (5, 5, InitialPieces(Map(King -> 1, Queen -> 1, Bishop -> 1, Knight -> 1))),
    (6, 6, InitialPieces(Map(King -> 2, Queen -> 2, Bishop -> 2, Knight -> 1)))
  )
  
  boardConfigs.foreach { case (m, n, pieces) =>
    println(s"Collecting data from ${m}x${n} board...")
    val collector = TrainingDataCollector(m, n, pieces)
    val data = collector.collectTrainingData()
    allTrainingData ++= data
    println(s"${m}x${n}: ${data.count(_.successful)} positive, ${data.count(!_.successful)} negative examples")
  }
  
  // Save combined dataset
  val writer = new PrintWriter(new File("chess_training_data.csv"))
  writer.println("boardSize,occupiedSquares,remainingPieces,pieceType,candidateX,candidateY,attackedSquares,safeSquares,cornerDistance,centerDistance,nearbyPieces,threatenedByExisting,threatensExisting,successful")
  
  allTrainingData.foreach { example =>
    writer.println(s"${example.boardSize},${example.occupiedSquares},${example.remainingPieces},${example.pieceType},${example.candidateX},${example.candidateY},${example.attackedSquares},${example.safeSquares},${example.cornerDistance},${example.centerDistance},${example.nearbyPieces},${example.threatenedByExisting},${example.threatensExisting},${example.successful}")
  }
  
  writer.close()
  
  val totalExamples = allTrainingData.size
  val positiveExamples = allTrainingData.count(_.successful)
  val negativeExamples = totalExamples - positiveExamples
  
  println(s"Total training data: $totalExamples examples")
  println(s"Positive examples: $positiveExamples")
  println(s"Negative examples: $negativeExamples")
  println(s"Balance ratio: ${positiveExamples.toDouble / totalExamples * 100}% positive")
  println("Saved to chess_training_data.csv")
}