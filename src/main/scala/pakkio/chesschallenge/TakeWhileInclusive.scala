package pakkio.chesschallenge

// this is an interesting addition to Iterator allowing to do takeWhile including the first non matching element
// it can be very useful for instance in dealing with shadowing when we follow a ray from a piece
// understanding when the first slot is not free...
class IteratorExtension[A](i : Iterator[A]) {
  def takeWhileInclusive(p: A => Boolean) = {
    val (a, b) = i.span(p)
    a ++ (if (b.hasNext) Some(b.next) else None)
  }
}

object ImplicitIterator {
  implicit def extendIterator[A](i : Iterator[A]) = new IteratorExtension(i)
}