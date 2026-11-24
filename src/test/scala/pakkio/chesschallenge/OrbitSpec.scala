package pakkio.chesschallenge

import org.scalatest.funsuite.AnyFunSuite

/** Plain bitboard DFS that counts EVERY solution, with no symmetry reduction.
  *
  * It shares no code with the solvers under test (own attack masks, own search), so its totals can
  * be used as ground truth for them. Pieces of the same kind are indistinguishable: squares are
  * chosen in increasing order within a kind. Sliding pieces need no blocking logic: any piece on a
  * line is already attacked by the first one, so such a placement is rejected anyway.
  */
object IndependentCounter {
  // kinds: 0 = King, 1 = Queen, 2 = Bishop, 3 = Knight
  def count(m: Int, n: Int, kings: Int, queens: Int, bishops: Int, knights: Int): Long = {
    require(m * n <= 64)
    val squares = m * n
    def bit(x: Int, y: Int): Long = if (x < 0 || y < 0 || x >= m || y >= n) 0L else 1L << (x * n + y)

    val mask = Array.ofDim[Long](4, squares)
    val dirs = Seq((1, 0), (-1, 0), (0, 1), (0, -1), (1, 1), (1, -1), (-1, 1), (-1, -1))
    val jumps = Seq((1, 2), (2, 1), (-1, 2), (-2, 1), (1, -2), (2, -1), (-1, -2), (-2, -1))
    for (x <- 0 until m; y <- 0 until n) {
      val i = x * n + y
      for ((dx, dy) <- dirs) mask(0)(i) |= bit(x + dx, y + dy)
      for ((dx, dy) <- dirs; s <- 1 until math.max(m, n)) {
        val b = bit(x + dx * s, y + dy * s)
        mask(1)(i) |= b
        if (dx != 0 && dy != 0) mask(2)(i) |= b
      }
      for ((dx, dy) <- jumps) mask(3)(i) |= bit(x + dx, y + dy)
    }

    val perKind = Array(kings, queens, bishops, knights)
    val order = Array(1, 2, 0, 3).filter(perKind(_) > 0) // queens first: they prune the most

    def dfs(oi: Int, left: Int, from: Int, occupied: Long, attacked: Long): Long =
      if (left == 0) {
        if (oi + 1 == order.length) 1L
        else dfs(oi + 1, perKind(order(oi + 1)), 0, occupied, attacked)
      } else {
        val kind = order(oi)
        var total = 0L
        var s = from
        while (s < squares) {
          val b = 1L << s
          if (((occupied | attacked) & b) == 0 && (mask(kind)(s) & occupied) == 0)
            total += dfs(oi, left - 1, s + 1, occupied | b, attacked | mask(kind)(s))
          s += 1
        }
        total
      }

    if (order.isEmpty) 1L else dfs(0, perKind(order(0)), 0, 0L, 0L)
  }
}

/** Orbit consistency of symmetry elimination.
  *
  * The solvers return canonical forms: one board per orbit of the 8 symmetries of the square.
  * "canonical x 8 = total" holds only if every orbit has 8 elements. On an odd board some
  * solutions are symmetric (orbit of 4 or 2), so 8 x canonical overcounts. The right total is the
  * sum of the orbit sizes, and it must match the independent count.
  */
class OrbitSpec extends AnyFunSuite {

  private val pieces = InitialPieces(Map(King -> 2, Queen -> 2, Bishop -> 2, Knight -> 1))

  /** Number of distinct boards obtained by applying the 8 symmetries of the square. */
  private def orbitSize(solution: Set[PieceAtSlot], n: Int): Int = {
    val transforms: Seq[(Int, Int) => (Int, Int)] = Seq(
      (x, y) => (x, y), (x, y) => (n - 1 - y, x), (x, y) => (n - 1 - x, n - 1 - y), (x, y) => (y, n - 1 - x),
      (x, y) => (n - 1 - x, y), (x, y) => (x, n - 1 - y), (x, y) => (n - 1 - y, n - 1 - x), (x, y) => (y, x)
    )
    transforms.map { f =>
      solution.map { p => val (nx, ny) = f(p.slot.x, p.slot.y); PieceAtSlot(p.piece, Slot(nx, ny)) }
    }.toSet.size
  }

  private def histogram(canonical: Set[Set[PieceAtSlot]], n: Int): Map[Int, Int] =
    canonical.toSeq.map(orbitSize(_, n)).groupBy(identity).map { case (size, all) => size -> all.size }

  test("independent counter agrees with known results") {
    assert(IndependentCounter.count(8, 8, 0, 8, 0, 0) === 92L) // 8 queens
    assert(IndependentCounter.count(6, 6, 0, 6, 0, 0) === 4L) // 6 queens
  }

  test("6x6: every canonical form has an orbit of 8, so canonical x 8 is the exact total") {
    val total = IndependentCounter.count(6, 6, 2, 2, 2, 1)
    val canonical = UltraOptimizedSolution(6, 6, pieces).solution
    assert(total === 23752L)
    assert(histogram(canonical, 6) === Map(8 -> 2969))
    assert(canonical.size * 8L === total)
  }

  test("7x7: sum of orbit sizes of the canonical forms equals the independent total") {
    val total = IndependentCounter.count(7, 7, 2, 2, 2, 1)
    val canonical = UltraOptimizedSolution(7, 7, pieces).solution
    val orbits = histogram(canonical, 7)

    // Nothing lost (no orbit missing) and nothing counted twice (no orbit listed twice):
    assert(orbits.map { case (size, count) => size.toLong * count }.sum === total)
    assert(canonical.size === 382990)
    // 23 symmetric orbits of size 4 hold 92 solutions: that is the whole overcount of 8 x canonical.
    assert(orbits === Map(4 -> 23, 8 -> 382967))
    assert(canonical.size * 8L - total === 92L)
  }
}
