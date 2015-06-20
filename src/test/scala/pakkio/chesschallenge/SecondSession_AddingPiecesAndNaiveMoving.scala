package pakkio.chesschallenge

import org.scalatest.FunSuite

import scala.util.{Failure, Try}
class SecondSession_AddingPiecesAndNaiveMoving extends FunSuite {

  // this test ensures that we have a way to enumerate free slots in a board
  // and that we can add pieces to the board
  test("Given a board with some pieces in it find out a listing of available positions"){
    val b=Board(2,2)
    // empty board gives all the slots
    val list=b.availableSlots()
    assert(list == List(Slot(0,0),Slot(0,1),Slot(1,0),Slot(1,1)))
    val b1=b.addAPiece(PieceAtSlot(Rook,Slot(0,0)))
    val l2=b1.availableSlots()
    assert(l2 == List(Slot(0,1),Slot(1,0),Slot(1,1)))
    val bAll=b1
      .addAPiece(PieceAtSlot(Queen,Slot(0,1)))
      .addAPiece(PieceAtSlot(King,Slot(1,0)))
      .addAPiece(PieceAtSlot(Knight,Slot(1,1)))

    val lAll=bAll.availableSlots()
    assert(lAll.isEmpty)
  }

  // very naive testing just to set up attacks and some other
  test("Given a board we can check if a slot is attacked by king"){
    // place King on the top left
    val b=Board(3,2,List(PieceAtSlot(King,Slot(0,0))))
    assert(b.attacks(Slot(0,1)) == true)
    assert(b.attacks(Slot(0,2)) == false)
    assert(b.attacks(Slot(1,0)) == true)
    assert(b.attacks(Slot(1,1)) == true)


  }
  test("Test queen"){
    val b=Board(3,3,List(PieceAtSlot(Queen,Slot(0,0))))
    assert(b.attacks(Slot(2,2)) == true) // as a bishop
    assert(b.attacks(Slot(2,0))== true) // as a rook
    assert(b.attacks(Slot(2,1)) == false) // but not as a knight
  }
  
  test("Test knight"){
    val b=Board(4,4,List())
    val attacked=b.attackedSlots(PieceAtSlot(Knight,Slot(0,0)))
    assert(attacked==List(Slot(1,2),Slot(2,1)))
  }
  
  test("Test rook"){
    val b=Board(7,7,List())
    val attacked=b.attackedSlots(PieceAtSlot(Rook,Slot(1,1)))
     assert(attacked==List(
        Slot(2,1), Slot(3,1), Slot(4,1), Slot(5,1), Slot(6,1), 
        Slot(0,1), 
        Slot(1,2), Slot(1,3), Slot(1,4), Slot(1,5), Slot(1,6), 
        Slot(1,0)))
  }
  
  test("Test bishop"){
    val b=Board(7,7,List())
    val attacked=b.attackedSlots(PieceAtSlot(Bishop,Slot(1,1)))
    assert(attacked==List(
        Slot(2,2), Slot(3,3), Slot(4,4), Slot(5,5), Slot(6,6), 
        Slot(0,0), 
        Slot(0,2), 
        Slot(2,0)))
    
  }




}
