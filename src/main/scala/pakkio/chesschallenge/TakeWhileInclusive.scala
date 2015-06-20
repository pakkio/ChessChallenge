package pakkio.chesschallenge

class IteratorExtension[A](i : Iterator[A]) {
  def takeWhileInclusive(p: A => Boolean) = {
    val (a, b) = i.span(p)
    a ++ (if (b.hasNext) Some(b.next) else None)
  }
}

object ImplicitIterator {
  implicit def extendIterator[A](i : Iterator[A]) = new IteratorExtension(i)
}