package pakkio.chesschallenge

/**
 * Created by pakki_000 on 16/06/2015.
 */
case class Board(m:Int,n:Int)

case class Pieces(list:Map[Symbol, Int])

case class Game(b:Board, pieces:Pieces){
  def validate:Boolean = ???
}

