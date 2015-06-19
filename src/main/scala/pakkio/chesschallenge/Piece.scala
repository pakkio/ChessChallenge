package pakkio.chesschallenge

abstract sealed class Piece {
  // each piece can define some paths in a board where to move
  def getPaths(b: Board, starting: Slot): List[List[Slot]]
}
class King extends Piece {
  // King returns up to 7 possible paths
  def getPaths(b: Board, starting: Slot) = {
    val l = for {
      x <- starting.x - 1 to starting.x + 1
      y <- starting.y - 1 to starting.y + 1
      
      slot = Slot(x, y)
      if ( !(x == starting.x && y== starting.y) && b.isValidPosition(slot))
      p = List(slot)
    } yield p
    l.toList
  }
}
trait RookAlike extends Piece {
  // a rook can have up to 4 possible paths
  def getPaths(b: Board, starting: Slot) = {

    val l1 = for {
      i <- starting.x + 1 until b.m
      slot = Slot(i, starting.y)
      if (b.isValidPosition(slot))
    } yield (slot)
    
    val l2 = for {
      i <- 0 until starting.x
      slot = Slot(i, starting.y)
      if (b.isValidPosition(slot))
    } yield (slot)
    
    val l3 = for {
      j <- starting.y + 1 until b.n
      slot = Slot(starting.x,j)
      if (b.isValidPosition(slot))
    } yield (slot)
    
    val l4 = for {
      j <- 0 until starting.y
      slot = Slot(starting.x,j)
      if (b.isValidPosition(slot))
    } yield (slot)
    
    
    List(l1.toList,l2.toList,l3.toList,l4.toList)
  }
}
class Rook extends RookAlike 

trait BishopAlike extends Piece {
  def getPaths (b: Board, starting: Slot) = {
    val l1 = for {
      i <- starting.x + 1 until Math.max(b.m,b.n)
      delta = i - starting.x
      slot = Slot(i, starting.y + delta )
      if (b.isValidPosition(slot))
    } yield (slot)
    
    val l2 = for {
      i <- 0 until starting.x
      delta = starting.x - i
      slot = Slot(i, starting.y - delta)
      if (b.isValidPosition(slot))
    } yield (slot)
    
    val l3 = for {
      j <- starting.y + 1 until Math.max(b.n,b.m)
      delta = starting.y - j
      slot = Slot(starting.x + delta ,j)
      if (b.isValidPosition(slot))
    } yield (slot)
    
    val l4 = for {
      j <- 0 until starting.y
      delta = starting.y - j
      slot = Slot(starting.x - delta,j)
      if (b.isValidPosition(slot))
    } yield (slot)
    
    
    List(l1.toList,l2.toList,l3.toList,l4.toList)
  }
}
class Bishop extends BishopAlike

class Knight extends Piece {
  // Knight can move at "L" so precomputing all the positions where it can go
  def getPaths(b: Board, starting: Slot) = {
    val knightDirs = Seq((1, 2), (2, 1), (-1, 2), (-2, 1), (1, -2), (2, -1), (-1, -2), (-2, -1))
     
    val l = for {
      (i,j) <- knightDirs
      
      slot = Slot(starting.x+i, starting.y+j)
      if(b.isValidPosition(slot))
      
    } yield (List(slot))
    l.toList
  
  }
    
}
class Queen extends RookAlike with BishopAlike {
  // Queen has a multiple inheritance approach to path:
  // we combine the 4 orthogonal paths of rook with the 4 diagonal paths of Bishop
  override def getPaths (b: Board, starting: Slot) =
  super[RookAlike].getPaths(b, starting) ++ super[BishopAlike].getPaths(b, starting)
}
object Knight 

