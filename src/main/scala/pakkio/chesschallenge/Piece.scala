package pakkio.chesschallenge

abstract sealed class Piece {
  val mnemonic:String
  // piece define a list of slot attacking not including its original position
  def getAttackedSlots(b: Board, starting: Slot): Set[Slot]

  // the attacked place is acceptable if it is in the board bounds
  // and is not exactly the starting slot of this originating attacker
  def isAcceptable(b:Board,starting:Slot,x:Slot) = {
    val ret = !(x.x == starting.x && x.y == starting.y) && b.isValidPosition(x)
    ret
  }
}
object King extends Piece {
  override val mnemonic="K"
  // King returns up to 7 possible paths
  def getAttackedSlots(b: Board, starting: Slot) = {
    val l = for {
      x <- starting.x - 1 to starting.x + 1
      y <- starting.y - 1 to starting.y + 1
      slot = Slot(x, y)
      if isAcceptable(b,starting,slot)
    } yield slot
    //println("King attacking "+l.mkString)
    l.toSet
  }
}
trait RookAlike extends Piece {
  override val mnemonic="R"
  // a rook can move horizontally and vertically
  def getAttackedSlots(b: Board, starting: Slot) = {

    // horizontal line
    val l1 = for {
      i <- 0 until b.m
      slot = Slot(i, starting.y)
      if isAcceptable(b,starting,slot)
    } yield slot

    // vertical line
    val l2 = for {
      j <- 0 until b.n
      slot = Slot(starting.x,j)
      if isAcceptable(b,starting,slot)
    } yield slot

    l1.toSet ++ l2.toSet
  }
}
object Rook extends RookAlike 

trait BishopAlike extends Piece {
  override val mnemonic = "B"
  def getAttackedSlots (b: Board, starting: Slot) = {
    
    // bishop moves along diagonals
    //
    val l1 = for {
      i <- starting.x  until Math.max(b.m,b.n)
      delta = i - starting.x
      slot = Slot(i, starting.y + delta )
      if isAcceptable(b,starting,slot)
      
    } yield slot
    
    // moving on the lower left diagonal
    val l2 = for {
      i <- starting.x -1 to 0 by -1
      delta = starting.x - i
      slot = Slot(i, starting.y - delta)
      if isAcceptable(b,starting,slot)
    } yield slot
    
    // moving on the top left diagonal
    val l3 = for {
      j <- starting.y until Math.max(b.n,b.m)
      delta = starting.y - j
      slot = Slot(starting.x + delta ,j)
      if isAcceptable(b,starting,slot)
    } yield slot
    
    // moving on the right bottom diagonal
    val l4 = for {
      j <- starting.y -1  to 0 by -1
      delta = starting.y - j
      slot = Slot(starting.x + delta,j)
      if isAcceptable(b,starting,slot)
    } yield slot
    
    
    val ret=l1.toSet ++ l2.toSet ++ l3.toSet ++ l4.toSet
    
    ret
  }


}
object Bishop extends BishopAlike

object Knight extends Piece {
  override val mnemonic="N"
  // Knight can move at "L" so pre-computing all the positions where it can go
  def getAttackedSlots(b: Board, starting: Slot) = {
    val knightDirs = Seq((1, 2), (2, 1), (-1, 2), (-2, 1), (1, -2), (2, -1), (-1, -2), (-2, -1))
     
    val l = for {
      (i,j) <- knightDirs
      
      slot = Slot(starting.x+i, starting.y+j)
      if b.isValidPosition(slot)
      
    } yield slot
    //println("Knight attacking "+l.mkString)
    l.toSet
  
  }
    
}
object Queen extends RookAlike with BishopAlike {
  override val mnemonic="Q"
  // Queen has a multiple inheritance approach to path:
  // we combine the 4 orthogonal paths of rook with the 4 diagonal paths of Bishop
  override def getAttackedSlots (b: Board, starting: Slot) =
  super[RookAlike].getAttackedSlots(b, starting) ++ super[BishopAlike].getAttackedSlots(b, starting)
}


