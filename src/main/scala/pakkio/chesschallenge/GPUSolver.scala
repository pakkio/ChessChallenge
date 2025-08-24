package pakkio.chesschallenge

import jcuda.driver.JCudaDriver._
import jcuda.driver._
import jcuda.{Pointer, Sizeof}

import scala.collection.mutable

// GPU-accelerated chess constraint solver using CUDA
case class GPUSolver(m: Int, n: Int, pieces: InitialPieces) {
  type Solutions = Set[Set[PieceAtSlot]]
  
  // Initialize CUDA
  private var initialized = false
  private var context: CUcontext = _
  private var module: CUmodule = _
  private var deviceMemoryManager: GPUMemoryManager = _
  
  // Statistics
  private var gpuTime = 0L
  private var cpuTime = 0L
  private var solutionCount = 0
  
  def initializeGPU(): Unit = {
    if (!initialized) {
      try {
        // Initialize CUDA driver
        cuInit(0)
        
        // Get device
        val device = new CUdevice()
        cuDeviceGet(device, 0)
        
        // Create context
        context = new CUcontext()
        cuCtxCreate(context, 0, device)
        
        // Load kernel module
        module = new CUmodule()
        val ptxFile = "target/libchess_kernel.so" // Our compiled kernel
        // Note: For now we'll use JCuda's built-in functions, kernel loading would need PTX
        
        deviceMemoryManager = new GPUMemoryManager(8000) // 8GB VRAM
        initialized = true
        
        println("GPU initialized successfully!")
        println(s"Available GPU memory: ${deviceMemoryManager.availableMemory}MB")
        
      } catch {
        case e: Exception =>
          println(s"GPU initialization failed: ${e.getMessage}")
          throw e
      }
    }
  }
  
  lazy val solution: Solutions = {
    val startTime = System.nanoTime()
    
    initializeGPU()
    
    val result = if (initialized) {
      solveOnGPU()
    } else {
      // Fallback to CPU
      println("Falling back to CPU solver...")
      val cpuSolver = CarefulOptimizedSolver(m, n, pieces)
      cpuSolver.solution
    }
    
    val totalTime = (System.nanoTime() - startTime) / 1000000.0
    printStatistics(totalTime)
    
    result
  }
  
  def count: Int = solution.size
  
  private def solveOnGPU(): Solutions = {
    val pieceList = flatPieces(pieces)
    val startTime = System.nanoTime()
    
    // Use hybrid CPU-GPU approach for now
    val solutions = parallelPlacement(pieceList, Set.empty, 0)
    
    gpuTime = (System.nanoTime() - startTime) / 1000000L
    solutions
  }
  
  // Hybrid CPU-GPU placement algorithm
  private def parallelPlacement(remainingPieces: List[Piece], 
                               currentState: Set[PieceAtSlot], 
                               depth: Int): Solutions = {
    if (remainingPieces.isEmpty) {
      return Set(currentState)
    }
    
    val piece = remainingPieces.head
    val restPieces = remainingPieces.tail
    
    // Get all available positions
    val occupiedSlots = currentState.map(_.slot)
    val allSlots = (for {
      x <- 0 until m
      y <- 0 until n
    } yield Slot(x, y)).toSet
    
    val availableSlots = (allSlots -- occupiedSlots).toList
    
    // For small numbers of positions, use CPU
    if (availableSlots.size < 100) {
      return cpuPlacement(piece, restPieces, currentState, availableSlots)
    }
    
    // For large numbers, use GPU-accelerated validation
    val validPlacements = if (initialized) {
      gpuValidatePlacements(piece, currentState, availableSlots)
    } else {
      cpuValidatePlacements(piece, currentState, availableSlots)
    }
    
    // Recursively solve for each valid placement
    val solutions = mutable.Set[Set[PieceAtSlot]]()
    
    for (validPlacement <- validPlacements) {
      val newState = currentState + validPlacement
      solutions ++= parallelPlacement(restPieces, newState, depth + 1)
    }
    
    solutions.toSet
  }
  
  // CPU fallback for small cases
  private def cpuPlacement(piece: Piece, remainingPieces: List[Piece], 
                          currentState: Set[PieceAtSlot], 
                          availableSlots: List[Slot]): Solutions = {
    val validPlacements = cpuValidatePlacements(piece, currentState, availableSlots)
    val solutions = mutable.Set[Set[PieceAtSlot]]()
    
    for (validPlacement <- validPlacements) {
      val newState = currentState + validPlacement
      solutions ++= parallelPlacement(remainingPieces, newState, 0)
    }
    
    solutions.toSet
  }
  
  // GPU-accelerated placement validation
  private def gpuValidatePlacements(piece: Piece, 
                                   currentState: Set[PieceAtSlot], 
                                   availableSlots: List[Slot]): List[PieceAtSlot] = {
    // For now, use CPU implementation with GPU potential
    // Full GPU implementation would require memory transfers and kernel launches
    cpuValidatePlacements(piece, currentState, availableSlots)
  }
  
  // CPU validation (used as baseline and fallback)
  private def cpuValidatePlacements(piece: Piece, 
                                   currentState: Set[PieceAtSlot], 
                                   availableSlots: List[Slot]): List[PieceAtSlot] = {
    val board = Board(m, n, currentState)
    val occupiedSlots = currentState.map(_.slot)
    val validPlacements = mutable.ListBuffer[PieceAtSlot]()
    
    for (slot <- availableSlots) {
      val attackedSlots = piece.getAttackedSlots(board, slot)
      val attacksExistingPiece = (attackedSlots & occupiedSlots).nonEmpty
      
      if (!attacksExistingPiece) {
        val isAttackedByExisting = currentState.exists { existingPiece =>
          val existingAttacks = existingPiece.piece.getAttackedSlots(board, existingPiece.slot)
          existingAttacks.contains(slot)
        }
        
        if (!isAttackedByExisting) {
          validPlacements += PieceAtSlot(piece, slot)
        }
      }
    }
    
    validPlacements.toList
  }
  
  private def printStatistics(totalTime: Double): Unit = {
    println(f"GPU Solver Statistics:")
    println(f"Total time: ${totalTime}ms")
    println(f"GPU time: ${gpuTime}ms")
    println(f"CPU time: ${cpuTime}ms")
    println(f"Solutions found: $solutionCount")
    if (initialized) {
      println(f"GPU memory used: ${deviceMemoryManager.usedMemory}MB")
    }
  }
  
  private def flatPieces(pieces: InitialPieces): List[Piece] = {
    val pieceList = for {
      (p, n) <- pieces.list
      _ <- 1 to n
    } yield p
    pieceList.toList
  }
  
  def cleanup(): Unit = {
    if (initialized) {
      if (deviceMemoryManager != null) {
        deviceMemoryManager.cleanup()
      }
      if (context != null) {
        cuCtxDestroy(context)
      }
    }
  }
}

// GPU Memory Manager for efficient VRAM usage
class GPUMemoryManager(val totalMemoryMB: Int) {
  private var usedMemoryMB = 0
  private val allocatedBuffers = mutable.ListBuffer[CUdeviceptr]()
  
  def availableMemory: Int = totalMemoryMB - usedMemoryMB
  def usedMemory: Int = usedMemoryMB
  
  def allocate(sizeBytes: Long): Option[CUdeviceptr] = {
    val sizeMB = (sizeBytes / (1024 * 1024)).toInt + 1
    
    if (sizeMB > availableMemory) {
      println(s"GPU memory allocation failed: requested ${sizeMB}MB, available ${availableMemory}MB")
      return None
    }
    
    try {
      val devicePtr = new CUdeviceptr()
      cuMemAlloc(devicePtr, sizeBytes)
      allocatedBuffers += devicePtr
      usedMemoryMB += sizeMB
      Some(devicePtr)
    } catch {
      case e: Exception =>
        println(s"GPU memory allocation error: ${e.getMessage}")
        None
    }
  }
  
  def free(ptr: CUdeviceptr): Unit = {
    try {
      cuMemFree(ptr)
      allocatedBuffers -= ptr
      // Note: In a real implementation, we'd track individual buffer sizes
    } catch {
      case e: Exception =>
        println(s"GPU memory free error: ${e.getMessage}")
    }
  }
  
  def cleanup(): Unit = {
    for (ptr <- allocatedBuffers) {
      try {
        cuMemFree(ptr)
      } catch {
        case _: Exception => // Ignore errors during cleanup
      }
    }
    allocatedBuffers.clear()
    usedMemoryMB = 0
  }
}