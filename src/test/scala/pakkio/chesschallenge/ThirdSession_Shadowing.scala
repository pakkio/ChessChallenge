package pakkio.chesschallenge

import org.scalatest.FunSuite

import scala.util.{ Failure, Try }
/**
 * Created by pakki_000 on 18/06/2015.
 */
class ThirdSession_Shadowing extends FunSuite { 

  test("ensure 'shadowing' from a Rook") {
    // in this case we have the following situation
    //  -f--
    //  -B--
    //  tRtt
    //  -t-- 
    val b = Board(4, 3, content = List(
      PieceAtSlot(Rook, Slot(1, 1)),
      PieceAtSlot(Bishop, Slot(1, 2))))
    // now includes also the Bishop because of takeWhileInclusive
    assert(b.attackedSlots(PieceAtSlot(Rook, Slot(1, 1)))==
      List(Slot(2,1), Slot(3,1), Slot(0,1), Slot(1,2), Slot(1,0)))
  }
  test("ensure 'shadowing' from a Bishop") {
    // in this case we have the following situation
    //  -- t-- 
    //  t-t--- 
    //  -B----
    //  t-R--- 
    //  --f--- 
    val b = Board(7, 7, content = List(
      PieceAtSlot(Rook, Slot(2, 1)),
      PieceAtSlot(Bishop, Slot(1, 2))))
    assert(b.attackedSlots(PieceAtSlot(Bishop, Slot(1, 2)))==
      List(Slot(2,3), Slot(3,4), Slot(4,5), Slot(5,6), 
          Slot(0,1), 
          Slot(0,3),
          Slot(3,0), Slot(2,1)))
  }
  
   test("ensure 'shadowing' from a Queen") {
    // in this case we have the following situation
    //  t-t-t- 
    //  -ttt--- 
    //  --Q---
    //  -ttR-- 
    //  t-t-f- 
    val b = Board(7, 7, content = List(
      PieceAtSlot(Rook, Slot(3, 1)),
      PieceAtSlot(Queen, Slot(2, 2))))
    assert(b.attackedSlots(PieceAtSlot(Queen, Slot(2, 2)))==
      List(Slot(3,2), Slot(4,2), Slot(5,2), Slot(6,2), 
          Slot(1,2), Slot(0,2), 
          Slot(2,3), Slot(2,4), Slot(2,5), Slot(2,6), 
          Slot(2,1), Slot(2,0), 
          Slot(3,3), Slot(4,4), Slot(5,5), Slot(6,6), 
          Slot(1,1), Slot(0,0), 
          Slot(1,3), Slot(0,4), Slot(4,0), Slot(3,1)))
    
  }

}
