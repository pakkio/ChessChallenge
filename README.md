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


I tried to develop in a TDD fashion i.e. writing down some tests using already supposed classes and methods and expected behaviour observed.
Also implementation was made in incremental steps so to have minimal simpler naive implementation even if potentially wrong and trying to better them up using only functional strategies.

I tried to avoid any mutable vars and always work on list of elements. For instance to model the attacked slots from a piece, I solved using the following consecutive flow:

* for each piece I find the possible Paths coming from them as if they were "rays" i.e. starting from the starting position and going away
* Paths are finishing when next coordinate would be out of the board
* So each Piece can move along a set of Paths: List[List[Slot]] every List[Slot] being a Path
* This list of paths is then followed functionally until the slots are really free or we meet another piece
* an implicit implementation of TakeWhileInclusive on iterators exploiting the span command is used to do this 

Pieces hierarchy is modeled using multiple inheritance allowed by Scala in a very elegant way essentially for modeling the behaviour of Queen,
which logically inherits both the movement from Rook and Bishop.

NOTE:

In the time frame allowed (which is around 7-8 hours) I was able to find a general, yet not optimized solution to the challenge.
What is currently left-out:

* there should be some caching mechanism to avoid repeating the same computation over and over
* current implementation fails in providing an answer for the proposed input in an adequate time: with 5x5 board answer comes in 7 seconds, but if using a 6x6 board it seems using 15 minutes with around 200000 solutions), so I'm expecting that 7x7 would require at least some days
* requirements asked for "unique" layout, currently they are still to be filtered out, and this probably will further optimize timings.

* adding caches would probably add mutable entities in the structure, another way would be to really use some clustering parallel map-reduce mechanism.




Here a sample of the output of the "sbt test" command:


    >sbt test
    [info] Loading global plugins from <home>\.sbt\0.13\plugins
    [info] Loading project definition from <home>\IdeaProjects\ChessChallenge\project
    [info] Set current project to ChessChallenge (in build file:/<home>/IdeaProjects/ChessChallenge/)
    [info] Updating {file:/<home>/IdeaProjects/ChessChallenge/}chesschallenge...
    [info] Resolving jline#jline;2.12.1 ...
    [info] Done updating.
    [info] Compiling 3 Scala sources to <home>\IdeaProjects\ChessChallenge\target\scala-2.11\classes...
    [info] 'compiler-interface' not yet compiled for Scala 2.11.6. Compiling...
    [info]   Compilation completed in 7.984 s
    [warn] there was one feature warning; re-run with -feature for details
    [warn] one warning found
    [info] Compiling 4 Scala sources to <home>\IdeaProjects\ChessChallenge\target\scala-2.11\test-classes...
    Processing piece: pakkio.chesschallenge.Knight$@1d6caae
    Processing piece: pakkio.chesschallenge.Bishop$@10febdf
    Processing piece: pakkio.chesschallenge.Rook$@1df8ac6
    Processing piece: pakkio.chesschallenge.Queen$@1f8ec7
    Processing piece: pakkio.chesschallenge.King$@946a8d
    [info] FirstSession_SettingModel:
    [info] - Assuming we have a way to build up a board MxN
    [info] - Have a way to define a listing of chess pieces
    [info] ThirdSession_Shadowing:
    [info] - ensure 'shadowing' from a Rook
    [info] - ensure 'shadowing' from a Bishop
    [info] - ensure 'shadowing' from a Queen
    [info] SecondSession_AddingPiecesAndNaiveMoving:
    [info] - Given a board with some pieces in it find out a listing of available positions
    [info] - Given a board we can check if a slot is attacked by king
    [info] - Test queen
    [info] - Test knight
    [info] - Test rook
    [info] - Test bishop
    Number of elements in list : 16
     Kn Bi -- --
     -- -- -- Ro
     Ki -- -- --
     -- -- Qu --
    
     Kn Bi -- --
     -- -- -- Qu
     Ki -- -- --
     -- -- Ro --
    
     -- -- Qu --
     Ki -- -- --
     -- -- -- Ro
     Kn Bi -- --
    
     -- -- Ro --
     Ki -- -- --
     -- -- -- Qu
     Kn Bi -- --
    
     -- -- Ro --
     Qu -- -- --
     -- -- -- Bi
     -- Ki -- Kn
    
     -- -- Qu --
     Ro -- -- --
     -- -- -- Bi
     -- Ki -- Kn
    
     -- Ki -- Kn
     -- -- -- Bi
     Qu -- -- --
     -- -- Ro --
    
     -- Ki -- Kn
     -- -- -- Bi
     Ro -- -- --
     -- -- Qu --
    
     -- Qu -- --
     -- -- -- Ro
     Bi -- -- --
     Kn -- Ki --
    
     -- Ro -- --
     -- -- -- Qu
     Bi -- -- --
     Kn -- Ki --
    
     Kn -- Ki --
     Bi -- -- --
     -- -- -- Ro
     -- Qu -- --
    
     Kn -- Ki --
     Bi -- -- --
     -- -- -- Qu
     -- Ro -- --
    
     -- -- Bi Kn
     Qu -- -- --
     -- -- -- Ki
     -- Ro -- --
    
     -- -- Bi Kn
     Ro -- -- --
     -- -- -- Ki
     -- Qu -- --
    
     -- Ro -- --
     -- -- -- Ki
     Qu -- -- --
     -- -- Bi Kn
    
     -- Qu -- --
     -- -- -- Ki
     Ro -- -- --
     -- -- Bi Kn
    
    [info] FourthSession_CreatingAllCombinations:
    [info] - create all combinations with some queens and a King
    [info] Run completed in 994 milliseconds.
    [info] Total number of tests run: 12
    [info] Suites: completed 4, aborted 0
    [info] Tests: succeeded 12, failed 0, canceled 0, ignored 0, pending 0
    [info] All tests passed.
    [success] Total time: 14 s, completed 20-giu-2015 12.43.28
    




