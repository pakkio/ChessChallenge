package pakkio.chesschallenge

import org.scalatest.FunSuite

import scala.util.{Failure, Try}
class T02_AddingPiecesAndNaiveMoving extends FunSuite {

  // this test ensures that we have a way to enumerate free slots in a board
  // and that we can add pieces to the board
  test("Given a board with some pieces in it find out a listing of available positions"){
    val b=Board(2,2)
    // empty board gives all the slots
    val list=b.availableSlots
    assert(list == Set(Slot(0,0),Slot(0,1),Slot(1,0),Slot(1,1)))
    val b1=b.addAPiece(PieceAtSlot(Rook,Slot(0,0)))
    val l2=b1.availableSlots
    assert(l2 == Set(Slot(1,1)))
    val bAll=b1
      .addAPiece(PieceAtSlot(Queen,Slot(0,1)))
      .addAPiece(PieceAtSlot(King,Slot(1,0)))
      .addAPiece(PieceAtSlot(Knight,Slot(1,1)))

    val lAll=bAll.availableSlots
    assert(lAll.isEmpty)
  }

  // very naive testing just to set up attacks and some other
  test("Given a board we can check if a slot is attacked by king"){
    // place King on the top left
    val b=Board(3,2,Set(PieceAtSlot(King,Slot(0,0))))
    assert(b.unSafeSlots.contains(Slot(0,1)))
    assert(!b.unSafeSlots.contains(Slot(0,2)))
    assert(b.unSafeSlots.contains(Slot(1,0)))
    assert(b.unSafeSlots.contains(Slot(1,1)))


  }
  test("Test queen"){
    val b=Board(3,3,Set(PieceAtSlot(Queen,Slot(0,0))))
    assert(b.unSafeSlots == Set(Slot(1,0),Slot(2,0),Slot(0,1),Slot(0,2),Slot(1,1),Slot(2,2)))
  }
  
  test("Test knight"){
    val b=Board(4,4,Set(PieceAtSlot(Knight,Slot(0,0))))
    val attacked=b.unSafeSlots
    assert(attacked.contains(Slot(1,2)))
    assert(attacked.contains(Slot(2,1)))
  }
  
  test("Test rook"){
    val b=Board(3,3,Set(PieceAtSlot(Rook,Slot(1,1))))
    val attacked=b.unSafeSlots
     assert(attacked==Set(
        Slot(1,2), Slot(1,0), Slot(0,1), Slot(2,1)))
  }
  
  test("Test bishop"){
    val b=Board(3,3,Set(PieceAtSlot(Bishop,Slot(1,1))))
    val attacked=b.unSafeSlots
    assert(attacked==Set(
        Slot(2,2), Slot(0,0), Slot(0,2), Slot(2,0)))
    
  }
  test("safeness Knight and King 1"){
    val b=Board(3,3,Set(
      PieceAtSlot(Knight,Slot(1,1)),
      PieceAtSlot(King,Slot(2,1))))
    assertResult(false,"Knight and King close are unsafe")(b.isSafe)
  }

  test("safeness Knight and King 2"){
    val b=Board(3,3,Set(
      PieceAtSlot(Knight,Slot(0,1)),
      PieceAtSlot(King,Slot(2,1))))
    assertResult(true,"Knight and King close are safe")(b.isSafe)
  }

  test("safeness complex 1"){
    val b=Board(5,5,Set(
      PieceAtSlot(Queen,Slot(0,0)),
      PieceAtSlot(Knight,Slot(1,4)),
      PieceAtSlot(King,Slot(1,2)),
      PieceAtSlot(Bishop,Slot(2,4)),
      PieceAtSlot(Rook,Slot(4,1))))
    assertResult(true,"complex safe config ")(b.isSafe)
  }

  test("safeness complex 2"){
    val b=Board(5,5,Set(
      PieceAtSlot(Queen,Slot(0,0)),
      PieceAtSlot(Knight,Slot(1,4)),
      PieceAtSlot(King,Slot(1,2)),
      PieceAtSlot(Bishop,Slot(2,4)),
      PieceAtSlot(Rook,Slot(4,2))))
    assertResult(false,"complex unsafe config")(b.isSafe)
    assert(b.unsafePieces.map(_.piece) == Set(Rook,King))
  }




}
