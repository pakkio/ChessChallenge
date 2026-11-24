# ChessChallenge - Exponential Performance Optimization

## Overview
Chess piece placement solver achieving extraordinary performance improvements through systematic algorithmic optimization. Finds all valid non-attacking arrangements of chess pieces on M×N boards.

**Performance (measured; see "How these numbers were checked" below):**
- 6×6 Board: original 29.6 s (all 23,752 solutions) → **124ms** (2,969 canonical forms), 239× (`benchmark_results.txt`)
- 7×7 Board: **382,990 canonical forms in 1.1s** on the author's machine (2.4s on a 2-core Xeon); the exact total is **3,063,828** solutions
- Correctness: totals cross-checked against an independent counter (`OrbitSpec`)

## Optimization Journey

### Key Breakthrough: Phase 1 Symmetry Elimination
- **Theory**: Applied group theory (dihedral group D₄) to eliminate 8-way symmetric duplicates
- **Implementation**: Canonical form detection using concurrent hash map
- **Result**: 4.3× additional speedup while maintaining perfect correctness
- **Verification**: 23,752 original solutions = 2,969 canonical forms × 8 symmetries (6×6 board)
- **Caveat on 7×7**: the same ×8 does *not* hold there. 23 orbits are symmetric (size 4), so 8 × 382,990 = 3,063,920 overcounts the true total, 3,063,828, by 92

### Evolution Timeline
1. **Functional Foundation** (20-24s) - Pure functional implementation  
2. **Parallel Processing** (8.7s) - Multi-core optimization, 54% improvement
3. **Object Elimination** (1.9s) - Reduced Board instantiation by 87%
4. **Symmetry Enhancement** (353ms) - Revolutionary 4.3× speedup through mathematical insight
5. **Ultra Bitboard Optimization** (124ms) - Smashed overhead using machine-level bitwise operations and direct pruning, achieving 1.1s 7x7 solve time.

## How these numbers were checked
- **6×6 timings** come from `benchmark_results.txt` (single run, warm JVM): `Solution` 29,586 ms for all 23,752 solutions, `UltraOptimizedSolution` 123.6 ms for 2,969 canonical forms. The 239× therefore mixes two things: faster code, and enumerating one board per orbit instead of all eight.
- **Totals and orbits**: `OrbitSpec` compares the solvers with `IndependentCounter`, a plain bitboard DFS that shares no code with them. On 7×7 it finds 3,063,828 solutions in 382,990 orbits: 382,967 of size 8 and 23 of size 4. Run it with `sbt "testOnly pakkio.chesschallenge.OrbitSpec"` (about 5 s).
- **No measured baseline for the original on 7×7.** Earlier figures (40+ minutes, 2,100×) were computed against an estimate hard-coded in `Test7x7`, and are no longer claimed.
- **Where the time goes**: the independent counter, which counts but does not build boards, finds all 3,063,828 solutions in about 0.2 s on the same 2-core machine, against 2.4 s for `UltraOptimizedSolution`, which also returns `Set[Set[PieceAtSlot]]`. The two are not like for like, but the gap suggests the cost is mostly allocation and set building, not the search.

## Technical Documentation
- **Complete Analysis**: See `article.pdf` for comprehensive technical paper
- **AI Enhancement Plan**: `enhancement-ai.md` outlines future 20-50× improvements
- **Git History**: Full optimization evolution documented in commits


## Usage
```bash
sbt run                    # Run 6×6 board (124ms)
sbt "runMain pakkio.chesschallenge.Test7x7Ultra"  # Run 7×7 board (1.1s)
sbt "runMain pakkio.chesschallenge.Benchmark"     # Comparative benchmark
```

## Architecture
- **Functional Design**: Immutable data structures, comprehensive for-comprehensions
- **Scala Features**: Multiple inheritance for Queen (Rook + Bishop behavior)
- **Optimization**: Concurrent hash maps, parallel collections, lightweight safety checking
- **Mathematical Foundation**: Group theory for symmetry elimination

## Historical Note
Original implementation (2015): about 3 working days of work, 7×7 in roughly 27 minutes. These are recollections, not measurements, so no speedup is claimed against them.
Current optimization (2025) produces the 7×7 canonical forms in **1.1 seconds** on the author's machine.
