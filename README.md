# ChessChallenge
Testing Exercise proposed by TryCatchUs
The challenge asked essentially to layout a set of Chess Pieces (King, Queen, Bishop, Knight, Rook) 
defined by an input specifying the name of the piece and the quantity, and to find out all layouts on
a board M x N where each of them can stay without attacking each other.

I split up the problem in various progressive incrementally sessions which are documented in the test folder:

* 1st session essentially tried to find out some tentative model to represent Pieces, Board and input
* 2nd session essentially focused on some useful functions to get from Board to find out free squares (slots as I called them) and attacked slots.
* 3rd session focused on the movement of the pieces and on the "shadowing" i.e. attacked pieces which are protecting other pieces from attack
* 4th session tried to put everything together and adapt similar concepts learned from the N Queen famous functional problem to this much more complex situation.
* 5th session will be devoted to optimization and final touches (such as avoid duplicated configurations) to be able to have an answer for the given problem in a reasonable time (right now it can take many hours).
* 6th session to refactor everything in such a way to fullfill requirements

During the 5th session I realized that I actually over-engineered it since shadowing even if currently working was adding unnecessary cpu and memory load.
Also worked in optimizing other internal algorithms (without changing the original idea), but making sure they are not wasting too much cpu/memory.


I tried to develop in a TDD fashion i.e. writing down some tests using already supposed classes and methods and expected behaviour observed.
Also implementation was made in incremental steps so to have minimal simpler naive implementation even if potentially wrong and trying to better them up using only functional strategies.

I tried to avoid any mutable vars and always work on list of elements. For instance to model the attacked slots from a piece, I solved using the following consecutive flow:

* for each piece I find the possible Paths coming from them as if they were "rays" i.e. starting from the starting position and going away
* Paths are finishing when next coordinate would be out of the board
* So each Piece can move along a set of Paths: Set[Set[Slot]] every Set[Slot] being a Path
* This list of paths is then followed functionally until the slots are really free or we meet another piece (This has been removed in final implementation since it was not needed and was one of the causes for too much cpu/memory )
* an implicit implementation of TakeWhileInclusive on iterators exploiting the span command is used to do this (Not needed in final implementation, but quite interesting to know since in some cases you need this special kind of "filtering"

Pieces hierarchy is modeled using multiple inheritance allowed by Scala in a very elegant way essentially for modeling the behaviour of Queen,
which logically inherits both the movement from Rook and Bishop.

NOTE:

It took me the equivalent of 3 working days (or better 5 nights) to complete the challenge.

Here a sample of the output of the testing program with one of the possible solutions printed out of the computed 3063828 solutions:
(Test took around 28 minutes to complete analyzing around 19 100 000 possible dispositions.

     - K - - - - K
     - - - B - - -
     - - - - - Q -
     - - - - - - -
     - - - - - - -
     Q - - - - - -
     - - - - N - B



