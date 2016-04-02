package pakkio.pakkio.chessfunctional

import org.scalatest.FunSuite
import pakkio.sandbox.{CheckMemory, Testable}

import scala.collection.immutable.Nil

case class PiecePos(piece:Piece,pos:P)

object Duplicates {

  val _1G: Double = math.pow(10, 9)
  val _1M: Double = math.pow(10, 6)
  val _1K: Double = math.pow(10, 3)

  def fmt(x:Double) = f"$x%.1f"
  def f(x: Long): String = {
    if (x >= _1G) return fmt(x / _1G) + " G"
    if (x >= _1M) return fmt(x / _1M) + " M"
    if (x >= _1K) return fmt(x / _1K) + " K"
    x + "B"
  }
  var counter: Int = 0
  var counterDuplicates: Int = 0
  val map=new util.HashSet[String]

  def check(s:Scene) = {
    val key = s.toString
    if(map.contains(key)){
      // println("duplicated entry")
      counterDuplicates+=1
      if(counterDuplicates % 100000 == 0) {
        println(s"Duplicates ${f(counterDuplicates)}")
      }
      true
    } else {
      this.synchronized {
        map.add(key)
        counter += 1
        if (counter % 100000 == 0) {
          println(s"Adding ${f(counter)}")
        }
        false
      }
    }

  }
}

case class Scene (size: Int,
                  pieces:List[Piece],
                  initialSafe:List[P],
                  attack: (P,P) => Boolean) {

  def union(s: (P,P) => Boolean, t: (P,P) => Boolean): (P,P) => Boolean =
    (me,p) => s(me,p) || t(me,p)

  // creating a Scene with original pieces, original attack function
  // and a new Piece
  def insert(piece:Piece):List[Scene] = {
    val forList = for {
      p <- initialSafe
      tentative = piece.copy(name = piece.name, Some(p))

    } yield {
      if (pieces.forall( p => !tentative.attacks(tentative.getP,p.getP)) ) {
        val newSafe = initialSafe.filter(
          p => {
            val me = tentative.getP
            p != me && !tentative.attacks(me, p)
          }
        )
        val newScene = Scene(size, pieces :+ tentative, newSafe, union(attack, tentative.attacks))
        // avoid introducing duplicates
        if(Duplicates.check(newScene)) None
        else
        Some(newScene)
      }
      else None
    }
    val ret = forList.flatten

    ret
  }

  override def toString = {
    val maxX = size//pieces.foldLeft(0)((x:Int, y) => if (x > y.getP.x) x else y.getP.x)
    val maxY = size//pieces.foldLeft(0)((x:Int, y) => if (x > y.getP.y) x else y.getP.y)
    printBoard("",maxX,maxY)
  }


  def mkElement(p: Option[Piece]) = {
    p match {
      case None    => "-"
      case Some(p1) => p1.name
    }
  }

  def getPieceAtPosition(c: Int, r: Int) : Option[Piece] = {
    val filtered = pieces.filter( p => p.getP.x == c && p.getP.y == r)
    val ret = filtered match {
      case Nil => None
      case p::_ => Some(p)

    }
    ret
  }

  def mkLine(r: Int, max: Int):String = {
    val l = for {
      c <- 0 to max
      p = getPieceAtPosition(c, r)
    } yield mkElement(p)
    val ret=l.toList.mkString(" ") + "\n "
    ret

  }

  def printBoard(s: String="",maxX:Int,maxY:Int) =
  {
    val l = for {
      r <- maxY to 0 by -1
    } yield mkLine(r,maxX)
    val ret=l.toList.mkString(" ", "", "")
    ret
  }
}

class TestBoard extends FunSuite with CheckMemory with Testable {

  val label = "TestBoard"

  def getFreeSlots(m:Int): Seq[P] = {

    // an initial completely safe board m x n
    for {
      i <- 0 until m
      j <- 0 until m
    } yield P(i, j)
  }

  test("printing board") {
    class TestPieces extends WithPieces {
      override val size: Int = 2
      override val pieces: Seq[Piece] = Seq(new Rook(Some(P(0, 0))))
    }

    (new TestPieces).toString === " -- --\n Ro --\n"


  }

  test("test1") {
    val noAttacks : (P,P) => Boolean = (me,p) => false
    val K=7
    val list=List(Scene(K, List(), getFreeSlots(K), noAttacks))
    // 7x7 King -> 2 , Queen -> 2, Bishop -> 2, Knight -> 1

    val finalList = list.par
      .flatMap(s => s.insert(new Queen)).par
      .flatMap(s => s.insert(new Queen)).par
      .flatMap(s => s.insert(new Bishop)).par
      .flatMap(s => s.insert(new Bishop)).par
      .flatMap(s => s.insert(new King)).par
      .flatMap(s => s.insert(new King)).par
      .flatMap(s => s.insert(new Knight)).par


    //finalList.map(println)
    println(finalList.head)
    println(finalList.last)

    finalList.size === 3063828

    println(s"Found ${finalList.size} combinations")


  }
  test("show statistics") {
    showMemory
  }
}
