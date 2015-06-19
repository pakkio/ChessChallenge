package pakkio.chesschallenge

import org.scalatest.FunSuite

import scala.util.{ Failure, Try }
/**
 * Created by pakki_000 on 18/06/2015.
 */
class FourthSession_CreatingAllCombinations extends FunSuite { 
  
  type Solutions = List[Board]
  
  // recursive function to place all residual pieces
  def placePieces(l:List[Piece]): Solutions = {
    
    l match {
      case List() => { List(Board(3,3)) }
      case piece :: rest => 
        println("Processing piece: "+piece)
        for {
         b <- placePieces(rest)
        // find an available slot
         slot <- b.availableSlots()
         // only in a safe position of the board
         if(!b.attacks(slot))
         // and providing this piece is not attacking other pieces
         alreadyPresentPieces=b.content.map(_.slot)
         pAtSlot=PieceAtSlot(piece,slot)
         if(b.attackedSlots(pAtSlot).intersect(alreadyPresentPieces)==List())
        
      } yield b.addAPiece(pAtSlot)
    }
  }

  test("create all combinations with some queens") {
   val b=Board(3,3)
   val piecesToInsert:Pieces = Pieces(Map(new Queen -> 2))
   
   // obtain a list of all the pieces to insert from the map
   // flattening down the count to proper repetition of the piece
   val pieceList = for {
     (p,n) <- piecesToInsert.list
     counter <- 1 to n
     
   } yield p
   
   
   
   
   val result = placePieces(pieceList.toList)
     println(s"Number of elements in list : ${result.size}")
    println(result.toList)
  }

}
