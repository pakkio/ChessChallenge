package pakkio.chesschallenge


case class Slot(x:Int,y:Int){
  //println("Allocating slot "+x+"-"+y)
}



case class PieceAtSlot(piece:Piece, slot:Slot)
case class Pieces(list:Map[Piece,Int])

case class Board(m:Int,n:Int, content:List[PieceAtSlot]=Nil) {
  def attacks(s: Slot) = {
    val attacked = for {
      p <- content
      slot <- attackedSlots(p)
    } yield( slot )
    println(attacked)
    attacked.contains(s)
  }
  
  def attackedSlots(p: PieceAtSlot) : List[Slot] = {
    val piece=p.piece
    val s=p.slot
    
      // gets a path (List of Slots)
    piece.getPaths(this, s).flatten
      // currently don't handle shadowing
      // probably with stream span we can handle it
      // ==> path.map(slot => (slot,isValidPosition(s),))toStream.span()
      
   
  }


  // this returns an option if a piece is there
  def getPieceAtPosition(pFind:Slot):Option[Piece] = {
    content.filter( _.slot == pFind ).map(_.piece).headOption
  }
  // canMoveHere can also tell if a piece is there
  def isValidPosition(s:Slot)  = 
    s.x >= 0 && s.x < m && s.y>=0 && s.y<n
  
  // create a new Board with added piece
  def addAPiece(p:PieceAtSlot) = {
    Board(m,n,p::content)
  }

  def availableSlots() = for {
    i<- 0 until m
    j<- 0 until n
    pos = Slot(i,j)
    p = getPieceAtPosition(pos)
    if(p==None)
  } yield(pos)
}





