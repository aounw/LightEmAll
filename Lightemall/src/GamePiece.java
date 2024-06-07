import tester.*;
import java.awt.Color;
import javalib.worldimages.*;

//represents a single tile in the game
class GamePiece {
  // in logical coordinates, with the origin
  // at the top-left corner of the screen
  int row;
  int col;
  // whether this GamePiece is connected to the
  // adjacent left, right, top, or bottom pieces
  boolean left;
  boolean right;
  boolean top;
  boolean bottom;
  // whether the power station is on this piece
  boolean powerStation;
  boolean powered;

  GamePiece(
      int row, int col, boolean left, boolean right, boolean top, boolean bottom, 
      boolean ps, boolean p) {
    this.row = row;
    this.col = col;
    this.left = left;
    this.right = right;
    this.top = top;
    this.bottom = bottom;
    this.powerStation = ps;
    this.powered = p;
  }

  //rotates a gamePiece clockwise
  //EFFECT: updates the boolean 
  //        values for left, right, top and bottom
  void rotate(int rotations) {
    for (int i = 0; i < rotations; i++) {
      boolean tempLeft = this.left;
      boolean tempRight = this.right;
      boolean tempTop = this.top;
      boolean tempBottom = this.bottom;
      this.left = tempBottom;
      this.right = tempTop;
      this.top = tempLeft;
      this.bottom = tempRight;
    }
  }

  // Sets this GamePiece to be a power station depending on parameters
  //EFFECT: Updates the powerStation field to true if the row and column match
  void stationPowerUp(int pr, int pc) {
    if (this.row == pr && this.col == pc) {
      this.powerStation = true;
    } else {
      this.powerStation = false;
    }
  }

  //Powers up this GamePiece
  //EFFECT: Sets the powered field to true
  void powerUp() {
    this.powered = true;
  }

  // Moves the power station from this GamePiece to another GamePiece
  //EFFECT: Sets this GamePiece's powerStation to false, 
  // and calls stationPowerUp on the other GamePiece
  void moveStation(GamePiece other, int pr, int pc) {
    this.powerStation = false; //edit when done w BFS
    other.stationPowerUp(pr, pc);
  }


  //determines if two gamepieces have a connection
  boolean hasAnyConnection(GamePiece other, String dir) {
    boolean passes = (dir.equals("left") && this.left && other.right)
        || (dir.equals("right") && this.right && other.left)
        || (dir.equals("up") && this.top && other.bottom)
        || (dir.equals("down") && this.bottom && other.top);
    return passes;
  }


  //Determines the colour of a wire
  Color wireColor() {
    if (this.powered) {
      return new Color(255, 0, 0);
    } else {
      return new Color(0, 255, 0);
    }
  }

  //Generate an image of this, the given GamePiece.
  // - size: the size of the tile, in pixels
  // - wireWidth: the width of wires, in pixels
  // - wireColor: the Color to use for rendering wires on this
  // - hasPowerStation: if true, draws a fancy star on this tile to represent the power station
  WorldImage tileImage(int size, int wireWidth, Color wireColor, boolean hasPowerStation) {
    // Start tile image off as a blue square with a wire-width square in the middle,
    // to make image "cleaner" (will look strange if tile has no wire, but that can't be)
    WorldImage image = new OverlayImage(
        new RectangleImage(wireWidth, wireWidth, OutlineMode.SOLID, wireColor),
        new RectangleImage(size, size, OutlineMode.SOLID, Color.DARK_GRAY));
    WorldImage vWire = new RectangleImage(wireWidth, (size + 1) / 2, OutlineMode.SOLID, wireColor);
    WorldImage hWire = new RectangleImage((size + 1) / 2, wireWidth, OutlineMode.SOLID, wireColor);

    if (this.top) {
      image = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP, vWire, 0, 0, image);
    }
    if (this.right) {
      image = new OverlayOffsetAlign(AlignModeX.RIGHT, AlignModeY.MIDDLE, hWire, 0, 0, image);
    }
    if (this.bottom) {
      image = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM, vWire, 0, 0, image);
    }
    if (this.left) {
      image = new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.MIDDLE, hWire, 0, 0, image);
    }
    if (hasPowerStation) {
      image = new OverlayImage(
          new OverlayImage(
              new StarImage(size / 3, 7, OutlineMode.OUTLINE, new Color(255, 128, 0)),
              new StarImage(size / 3, 7, OutlineMode.SOLID, new Color(0, 255, 255))),
          image);
    }
    image = new OverlayImage(
        new RectangleImage(size, size, OutlineMode.OUTLINE, Color.BLACK), image);
    return image;
  }

  //returns if this GamePiece can connect to the piece on its left
  boolean isLeftOk() {
    return this.left && this.row > 0;
  }

  // returns if this GamePiece can connect to the piece on its right
  boolean isRightOk(int width) {
    return this.right && this.row < (width - 1);
  }

  // returns if this GamePiece can connect to the piece above it
  boolean isTopOk() {
    return this.top && this.col > 0;
  }

  // returns if this GamePiece can connect to the piece below it
  boolean isBottomOk(int height) {
    return this.bottom && this.col < (height - 1);
  }
}

// examples class for testing 
class ExamplesGamePiece {
  GamePiece rotateTester1;
  GamePiece piece;
  GamePiece poweredPiece1;
  GamePiece unpoweredPiece1;
  GamePiece edgeLeft;
  GamePiece edgeRight;
  GamePiece edgeTop;
  GamePiece edgeBottom;
  GamePiece middlePiece;
  int width = 5;  
  int height = 5;

  // initialises data
  void initData() {
    this.rotateTester1 = new GamePiece(0, 0, true, false, false, false, false, false);
    this.piece = new GamePiece(1, 1, false, false, false, false, false, false);
    this.poweredPiece1 = new GamePiece(0, 0, false, false, false, false, false, true);
    this.unpoweredPiece1 = new GamePiece(0, 0, false, false, false, false, false, false);
    this.edgeLeft = new GamePiece(0, 1, false, true, true, true, false, false);
    this.edgeRight = new GamePiece(4, 2, true, false, true, true, false, false);
    this.edgeTop = new GamePiece(2, 0, true, true, false, true, false, false);
    this.edgeBottom = new GamePiece(2, 4, true, true, true, false, false, false);
    this.middlePiece = new GamePiece(2, 2, true, true, true, true, false, false);
  }

  // test for the method rotate
  void testRotate(Tester t) {
    this.initData();
    t.checkExpect(this.rotateTester1.left, true);
    t.checkExpect(this.rotateTester1.top, false);
    this.rotateTester1.rotate(1);
    t.checkExpect(this.rotateTester1.left, false);
    t.checkExpect(this.rotateTester1.top, true);
    this.rotateTester1.rotate(4);
    t.checkExpect(this.rotateTester1.left, false);
    t.checkExpect(this.rotateTester1.top, true);
    this.rotateTester1.rotate(0);
    t.checkExpect(this.rotateTester1.left, false);
    t.checkExpect(this.rotateTester1.top, true);
  }

  // test for stationPowerUp
  void testStationPowerUp(Tester t) {
    this.initData();
    t.checkExpect(piece.powerStation, false);
    this.piece.stationPowerUp(1, 1);
    t.checkExpect(piece.powerStation, true);
    piece.stationPowerUp(2, 2);
    t.checkExpect(piece.powerStation, false);
  }

  // test for moveStation
  void testMoveStation(Tester t) {
    this.initData();
    GamePiece piece1 = new GamePiece(0, 0, false, false, false, false, true, false);
    GamePiece piece2 = new GamePiece(0, 1, false, false, false, false, false, false);
    piece1.moveStation(piece2, 0, 1);
    t.checkExpect(piece1.powerStation, false);
    t.checkExpect(piece2.powerStation, true);
  }

  // test for hasAnyConnection
  void testHasAnyConnection(Tester t) {
    this.initData();
    GamePiece piece1 = new GamePiece(0, 0, true, false, false, false, false, false);
    GamePiece piece2 = new GamePiece(0, 1, false, true, false, false, false, false);
    GamePiece piece3 = new GamePiece(0, 1, false, false, false, false, false, false);
    t.checkExpect(piece1.hasAnyConnection(piece2, "left"), true);
    t.checkExpect(piece1.hasAnyConnection(piece3, "right"), false);
    t.checkExpect(piece1.hasAnyConnection(piece2, "up"), false);
  }

  // test for wireColor
  void testWireColor(Tester t) {
    this.initData();
    t.checkExpect(poweredPiece1.wireColor(), new Color(255, 0, 0));
    t.checkExpect(unpoweredPiece1.wireColor(), new Color(0, 255, 0));
  }

  // test isLeftOk method
  void testIsLeftOk(Tester t) {
    this.initData();
    t.checkExpect(edgeLeft.isLeftOk(), false);   
    t.checkExpect(middlePiece.isLeftOk(), true); 
  }

  // test isRightOk method
  void testIsRightOk(Tester t) {
    this.initData();
    t.checkExpect(edgeRight.isRightOk(this.width), false);   
    t.checkExpect(middlePiece.isRightOk(this.width), true);  
  }

  // test isTopOk method
  void testIsTopOk(Tester t) {
    this.initData();
    t.checkExpect(edgeTop.isTopOk(), false);   
    t.checkExpect(middlePiece.isTopOk(), true); 
  }

  // test isBottomOk method
  void testIsBottomOk(Tester t) {
    this.initData();
    t.checkExpect(edgeBottom.isBottomOk(this.height), false); 
    t.checkExpect(middlePiece.isBottomOk(this.height), true);  
  }
}