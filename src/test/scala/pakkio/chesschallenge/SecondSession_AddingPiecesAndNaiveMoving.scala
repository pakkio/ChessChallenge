package pakkio.chesschallenge

import org.scalatest.FunSuite

import scala.util.{Failure, Try}
/**
 * Created by pakki_000 on 18/06/2015.
 */
class SecondSession_AddingPiecesAndNaiveMoving extends FunSuite {

  // this test ensures that we have a way to enumerate free slots in a board
  // and that we can add pieces to the board
  test("Given a board with some pieces in it find out a listing of available positions"){
    val b=Board(2,2)
    // empty board gives all the slots
    val list=b.availableSlots()
    assert(list == List(Slot(0,0),Slot(0,1),Slot(1,0),Slot(1,1)))
    val b1=b.addAPiece(PieceAtSlot(new Rook,Slot(0,0)))
    val l2=b1.availableSlots()
    assert(l2 == List(Slot(0,1),Slot(1,0),Slot(1,1)))
    val bAll=b1
      .addAPiece(PieceAtSlot(new Queen,Slot(0,1)))
      .addAPiece(PieceAtSlot(new King,Slot(1,0)))
      .addAPiece(PieceAtSlot(new Knight,Slot(1,1)))

    val lAll=bAll.availableSlots()
    assert(lAll == Nil)
  }

  // very naive testing just to set up attacks and some other
  test("Given a board we can check if a slot is attacked by king"){
    // place King on the top left
    val b=Board(3,2,List(PieceAtSlot(new King,Slot(0,0))))
    assert(b.attacks(Slot(0,1)) == true)
    assert(b.attacks(Slot(0,2)) == false)
    assert(b.attacks(Slot(1,0)) == true)
    assert(b.attacks(Slot(1,1)) == true)


  }
  test("Test queen"){
    val b=Board(3,3,List(PieceAtSlot(new Queen,Slot(0,0))))
    assert(b.attacks(Slot(2,2)) == true) // as a bishop
    assert(b.attacks(Slot(2,0))== true) // as a rook
    assert(b.attacks(Slot(2,1)) == false) // but not as a knight
  }
  
  test("Test knight"){
    val b=Board(3,3,List(PieceAtSlot(new Knight,Slot(0,0))))
    assert(b.attacks(Slot(1,2))==true)
    assert(b.attacks(Slot(2,1))==true)
    assert(b.attacks(Slot(1,1))==false)
  }
  
  test("Test rook"){
    val b=Board(3,3,List(PieceAtSlot(new Rook,Slot(1,1))))
    assert(b.attacks(Slot(0,0))==false)
    assert(b.attacks(Slot(0,1))==true)
    assert(b.attacks(Slot(1,0))==true)
  }




}
