import java.util.ArrayList;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.ArrayDeque;

//represents an edge 
class Edge {
  GamePiece fromNode;
  GamePiece toNode;
  int weight;

  Edge(GamePiece fromNode, GamePiece toNode, int weight) {
    this.fromNode = fromNode;
    this.toNode = toNode;
    this.weight = weight;
  }
}

//world class for the game 
class LightEmAll extends World {
  // a list of columns of GamePieces,
  // i.e., represents the board in column-major order
  ArrayList<ArrayList<GamePiece>> board;
  // a list of all nodes
  ArrayList<GamePiece> nodes;
  // a list of edges of the minimum spanning tree
  ArrayList<Edge> mst;
  // the width and height of the board
  int width;
  int height;
  // the current location of the power station,
  // as well as its effective radius
  int powerRow;
  int powerCol;
  int radius;
  Random rand;

  LightEmAll(int width, int height, Random rand) {
    this.board = new ArrayList<ArrayList<GamePiece>>();
    this.nodes = new ArrayList<GamePiece>();
    this.mst = new ArrayList<Edge>();
    this.width = width;
    this.height = height;
    this.powerRow = 0;
    this.powerCol = 0;
    this.radius = (width * height) / 3;
    this.rand = rand;
  }

  // Initialises the game board with interconnected GamePieces
  //EFFECT: fills in a row (nodes) with "width" amount of GamePieces
  //        Fills in a list of list of game pieces with "height" amount
  //        adds all the nodes into the list
  void initBoard() {
    for (int c = 0; c < this.height; c++) {
      ArrayList<GamePiece> rowList = new ArrayList<GamePiece>();
      for (int r = 0; r < this.width; r++) {
        GamePiece tempGame = 
            new GamePiece(r, c, false, false, false, false, false, false);
        tempGame.stationPowerUp(this.powerRow, this.powerCol);
        rowList.add(tempGame);
        this.nodes.add(tempGame);
      }
      this.board.add(rowList);
    }
  }

  //creates a list with every connection represented as an edge
  //generates a random weight from 0 to 100
  //sorts the list according to weight from smallest to largest
  ArrayList<Edge> createMSTWorklist(ArrayList<ArrayList<GamePiece>> b) {
    ArrayList<Edge> mstList = new ArrayList<Edge>();
    for (int c = 0; c < this.height; c++) {
      for (int r = 0; r < this.width; r++) {
        GamePiece gp = b.get(c).get(r);
        GamePiece next;
        Edge e;
        if (gp.row != 0) {
          next = b.get(c).get(r - 1);
          e = new Edge(gp, next, this.rand.nextInt(50));
          mstList.add(e);
        }
        if (gp.col != 0) {
          next = b.get(c - 1).get(r);
          e = new Edge(gp, next, this.rand.nextInt(50));
          mstList.add(e);
        }
        if (gp.row != this.width - 1) {
          next = b.get(c).get(r + 1);
          e = new Edge(gp, next, this.rand.nextInt(50));
          mstList.add(e);
        }
        if (gp.col != this.height - 1) {
          next = b.get(c + 1).get(r);
          e = new Edge(gp, next, this.rand.nextInt(50));
          mstList.add(e);
        }
      }
    }
    mstList.sort((e1, e2) -> (e1.weight - e2.weight));
    return mstList;
  }

  //finds the representative of a node
  GamePiece find(HashMap<GamePiece, GamePiece> representatives, GamePiece node) {
    GamePiece parent = representatives.get(node);
    if (parent == node) {
      return node;
    }
    return find(representatives, parent);
  }

  //uses Kruskal's algorithm to create a minimum spanning tree
  //EFFECT: Adds Edges into the mst list
  void buildMST(ArrayList<ArrayList<GamePiece>> b) {
    HashMap<GamePiece, GamePiece> representatives = new HashMap<GamePiece, GamePiece>();
    ArrayList<Edge> worklist = this.createMSTWorklist(b);

    for (GamePiece gp : this.nodes) {
      representatives.put(gp, gp);
    }

    while (worklist.size() > 0) {
      Edge cheapest = worklist.remove(0);
      GamePiece x = cheapest.fromNode;
      GamePiece y = cheapest.toNode;
      GamePiece xRep = this.find(representatives, x);
      GamePiece yRep = this.find(representatives, y);
      if (!xRep.equals(yRep)) {
        this.mst.add(cheapest);
        representatives.put(xRep, yRep);
      }
    }
  }

  //creates a connections between connected nodes in a tree
  //EFFECT: mutates the values of the fromNode and toNode
  void visualiseConnections() {
    for (Edge edge : this.mst) {
      GamePiece fromNode = edge.fromNode;
      GamePiece toNode = edge.toNode;

      if (fromNode.row > toNode.row) {
        fromNode.left = true;
        toNode.right = true;
      } else if (fromNode.row < toNode.row) {
        fromNode.right = true;
        toNode.left = true;
      } else if (fromNode.col > toNode.col) {
        fromNode.top = true;
        toNode.bottom = true;
      } else if (fromNode.col < toNode.col) {
        fromNode.bottom = true;
        toNode.top = true;
      }
    }
  }

  // Randomly rotates each GamePiece on the board
  //EFFECT: randomly rotates every GamePiece in the board
  //        initialises every gamepiece.powered to true if connected    
  void randRotateBoard() {
    for (int c = 0; c < this.height; c++) {
      for (int r = 0; r < this.width; r++) {
        GamePiece current = this.board.get(c).get(r);
        current.rotate(this.rand.nextInt(3));
      }
    }
    this.hasPath(this.board.get(this.powerCol).get(this.powerRow));
  }


  //Check if there is a path from the source to every other cell on the board
  //EFFECT: Updates powered status of GamePiece
  boolean hasPath(GamePiece source) {
    ArrayList<GamePiece> alreadySeen = new ArrayList<GamePiece>();
    ArrayDeque<GamePiece> worklist = new ArrayDeque<GamePiece>();

    for (int c = 0; c < this.height; c++) {
      for (int r = 0; r < this.width; r++) {
        this.board.get(c).get(r).powered = false;
      }
    }

    worklist.addFirst(source);

    while (!worklist.isEmpty()) {
      GamePiece next = worklist.removeFirst();
      //      next.powered = next.powerStation;

      // Process neighbors of the current GamePiece
      if (next.isLeftOk()) {
        GamePiece left = board.get(next.col).get(next.row - 1);
        if (!alreadySeen.contains(left) && next.hasAnyConnection(left, "left")) {
          worklist.addLast(left);
        }
      }
      if (next.isRightOk(this.width)) {
        GamePiece right = board.get(next.col).get(next.row + 1);
        if (!alreadySeen.contains(right) && next.hasAnyConnection(right, "right")) {
          worklist.addLast(right);
        }
      }
      if (next.isTopOk()) {
        GamePiece top = board.get(next.col - 1).get(next.row);
        if (!alreadySeen.contains(top) && next.hasAnyConnection(top, "up")) {
          worklist.addLast(top);
        }
      }
      if (next.isBottomOk(this.height)) {
        GamePiece bottom = board.get(next.col + 1).get(next.row);
        if (!alreadySeen.contains(bottom) && next.hasAnyConnection(bottom, "down")) {
          worklist.addLast(bottom);
        }
      }
      alreadySeen.add(next);
    }

    for (GamePiece gp : alreadySeen) {
      gp.powerUp();
    }
    // Check if all cells are visited
    return alreadySeen.size() == this.width * this.height;
  }


  //makes the scene to display the current grid
  public WorldScene makeScene() {
    int tileSize = 30;
    int wireWidth = 3;
    WorldScene ws = new WorldScene(this.width * tileSize, this.height * tileSize);
    WorldImage columnImage = new EmptyImage();
    for (int c = 0; c < this.height; c++) {
      WorldImage rowImage = new EmptyImage();
      for (int r = 0; r < this.width; r++) {
        GamePiece currPiece = this.board.get(c).get(r);
        Color currPieceColor = currPiece.wireColor();
        boolean hasPowerStation = (r == this.powerRow && c == this.powerCol);
        WorldImage tileImage = currPiece.tileImage(
            tileSize, wireWidth, currPieceColor, 
            hasPowerStation);
        rowImage = new BesideImage(rowImage, tileImage);
      }
      columnImage = new AboveImage(columnImage, rowImage);
    }
    ws.placeImageXY(columnImage, this.width * tileSize / 2, this.height * tileSize / 2);
    GamePiece source = this.board.get(this.powerCol).get(this.powerRow);
    if (this.hasPath(source)) {
      ws.placeImageXY(new TextImage("You Win, Good Job!", 
          Math.round((this.width * this.height) / 2), Color.WHITE), 
          Math.round((tileSize * this.width) / 2), 
          Math.round((tileSize * this.height) / 2));
    }
    return ws;
  }


  //Allows right and left button clicks
  //EFFECT: handles all the mouse clicks
  public void onMouseClicked(Posn pos, String buttonName) {
    GamePiece stationPiece = this.board.get(this.powerCol).get(this.powerRow);
    if (!this.hasPath(stationPiece)) {
      for (int c = 0; c < this.height; c++) {
        for (int r = 0; r < this.width; r++) {
          if (pos.x > 30 * r
              && pos.x < (30 * r) + 30
              && pos.y > 30 * c
              && pos.y < (30 * c) + 30) {
            GamePiece current = this.board.get(c).get(r);
            if (buttonName.equals("LeftButton")) {
              current.rotate(1);
            }
          }
        }
      }
      this.hasPath(this.board.get(this.powerCol).get(this.powerRow));
    }
  }


  //Allows right and left button clicks
  //EFFECT: handles all the mouse clicks
  public void onKeyEvent(String key) {
    GamePiece currentCell = this.board.get(this.powerCol).get(this.powerRow);
    if (!this.hasPath(currentCell)) {
      if (key.equals("left")) {
        if (this.powerRow > 0) {
          GamePiece left = this.board.get(this.powerCol).get(this.powerRow - 1);
          if (currentCell.hasAnyConnection(left, key)) {
            this.powerRow -= 1;
            currentCell.moveStation(left, this.powerRow, this.powerCol);
          }
        }
      }
      if (key.equals("right")) {
        if (this.powerRow < this.width - 1) {
          GamePiece right = this.board.get(this.powerCol).get(this.powerRow + 1);
          if (currentCell.hasAnyConnection(right, key)) {
            this.powerRow += 1;
            currentCell.moveStation(right, this.powerRow, this.powerCol);
          }
        }
      }
      if (key.equals("up")) {
        if (this.powerCol > 0) {
          GamePiece above = this.board.get(this.powerCol - 1).get(this.powerRow);
          if (currentCell.hasAnyConnection(above, key)) {
            this.powerCol -= 1;
            currentCell.moveStation(above, this.powerRow, this.powerCol);
          }
        }
      }
      if (key.equals("down")) {
        if (this.powerCol < this.height - 1) {
          GamePiece below = this.board.get(this.powerCol + 1).get(this.powerRow);
          if (currentCell.hasAnyConnection(below, key)) {
            this.powerCol += 1;
            currentCell.moveStation(below, this.powerRow, this.powerCol);
          }
        }
      }
      this.hasPath(currentCell);
    }
  }
}

//examples class for testing 
class ExamplesLightEmAll {
  LightEmAll worldTester1;
  LightEmAll worldTester2;
  LightEmAll game;

  //initialises the data for testing
  void initData() {
    this.worldTester1 = new LightEmAll(3, 3, new Random(2));
    this.worldTester2 = new LightEmAll(2, 2, new Random(2));
    this.game = new LightEmAll(8, 8, new Random());
    this.game.initBoard();
    this.game.buildMST(this.game.board);
    this.game.visualiseConnections();
    this.game.randRotateBoard();
  }

  //test for the big bang method
  void testBigBang(Tester t) {
    this.initData();
    LightEmAll world = this.game;
    int worldWidth = 30 * this.game.width;
    int worldHeight = 30 * this.game.height;
    double tickRate = .1;
    world.bigBang(worldWidth, worldHeight, tickRate);
  }

  //testing the initBoard method
  void testInitBoard(Tester t) {
    this.initData();
    t.checkExpect(this.worldTester1.board, 
        new ArrayList<ArrayList<GamePiece>>());
    this.worldTester1.initBoard();
    t.checkExpect(this.worldTester1.board, 
        new ArrayList<ArrayList<GamePiece>>(
            Arrays.asList(
                new ArrayList<GamePiece>(Arrays.asList(
                    new GamePiece(0, 0, false, false, false, false, true, false),
                    new GamePiece(1, 0, false, false, false, false, false, false),
                    new GamePiece(2, 0, false, false, false, false, false, false))),
                new ArrayList<GamePiece>(Arrays.asList(
                    new GamePiece(0, 1, false, false, false, false, false, false),
                    new GamePiece(1, 1, false, false, false, false, false, false),
                    new GamePiece(2, 1, false, false, false, false, false, false))),
                new ArrayList<GamePiece>(Arrays.asList(
                    new GamePiece(0, 2, false, false, false, false, false, false),
                    new GamePiece(1, 2, false, false, false, false, false, false),
                    new GamePiece(2, 2, false, false, false, false, false, false))))));
  }

  //testing the randRotateBoard method
  void testRandRotateBoard(Tester t) {
    this.initData();
    this.worldTester1.initBoard();
    this.worldTester1.buildMST(this.worldTester1.board);
    this.worldTester1.visualiseConnections();
    t.checkExpect(this.worldTester1.board, 
        new ArrayList<ArrayList<GamePiece>>(
            Arrays.asList(
                new ArrayList<GamePiece>(Arrays.asList(
                    new GamePiece(0, 0, false, true, false, true, true, false),
                    new GamePiece(1, 0, true, true, false, false, false, false),
                    new GamePiece(2, 0, true, false, false, true, false, false))),
                new ArrayList<GamePiece>(Arrays.asList(
                    new GamePiece(0, 1, false, false, true, true, false, false),
                    new GamePiece(1, 1, false, true, false, true, false, false),
                    new GamePiece(2, 1, true, false, true, true, false, false))),
                new ArrayList<GamePiece>(Arrays.asList(
                    new GamePiece(0, 2, false, false, true, false, false, false),
                    new GamePiece(1, 2, false, false, true, false, false, false),
                    new GamePiece(2, 2, false, false, true, false, false, false))))));
    this.worldTester1.randRotateBoard();
    t.checkExpect(this.worldTester1.board, 
        new ArrayList<ArrayList<GamePiece>>(
            Arrays.asList(
                new ArrayList<GamePiece>(Arrays.asList(
                    new GamePiece(0, 0, true, false, false, true, true, true),
                    new GamePiece(1, 0, true, true, false, false, false, false),
                    new GamePiece(2, 0, true, false, true, false, false, false))),
                new ArrayList<GamePiece>(Arrays.asList(
                    new GamePiece(0, 1, false, false, true, true, false, true),
                    new GamePiece(1, 1, false, true, false, true, false, false),
                    new GamePiece(2, 1, true, true, true, false, false, false))),
                new ArrayList<GamePiece>(Arrays.asList(
                    new GamePiece(0, 2, false, true, false, false, false, false),
                    new GamePiece(1, 2, false, false, true, false, false, false),
                    new GamePiece(2, 2, false, true, false, false, false, false))))));
  }

  //testing the hasPath method
  void testHasPath(Tester t) {
    this.initData();
    this.worldTester1.initBoard();
    this.worldTester1.buildMST(this.worldTester1.board);
    this.worldTester1.visualiseConnections();
    t.checkExpect(this.worldTester1.board, 
        new ArrayList<ArrayList<GamePiece>>(
            Arrays.asList(
                new ArrayList<GamePiece>(Arrays.asList(
                    new GamePiece(0, 0, false, true, false, true, true, false),
                    new GamePiece(1, 0, true, true, false, false, false, false),
                    new GamePiece(2, 0, true, false, false, true, false, false))),
                new ArrayList<GamePiece>(Arrays.asList(
                    new GamePiece(0, 1, false, false, true, true, false, false),
                    new GamePiece(1, 1, false, true, false, true, false, false),
                    new GamePiece(2, 1, true, false, true, true, false, false))),
                new ArrayList<GamePiece>(Arrays.asList(
                    new GamePiece(0, 2, false, false, true, false, false, false),
                    new GamePiece(1, 2, false, false, true, false, false, false),
                    new GamePiece(2, 2, false, false, true, false, false, false))))));
    this.worldTester1.hasPath(this.worldTester1.board.get(0).get(0));
    t.checkExpect(this.worldTester1.board, 
        new ArrayList<ArrayList<GamePiece>>(
            Arrays.asList(
                new ArrayList<GamePiece>(Arrays.asList(
                    new GamePiece(0, 0, false, true, false, true, true, true),
                    new GamePiece(1, 0, true, true, false, false, false, true),
                    new GamePiece(2, 0, true, false, false, true, false, true))),
                new ArrayList<GamePiece>(Arrays.asList(
                    new GamePiece(0, 1, false, false, true, true, false, true),
                    new GamePiece(1, 1, false, true, false, true, false, true),
                    new GamePiece(2, 1, true, false, true, true, false, true))),
                new ArrayList<GamePiece>(Arrays.asList(
                    new GamePiece(0, 2, false, false, true, false, false, true),
                    new GamePiece(1, 2, false, false, true, false, false, true),
                    new GamePiece(2, 2, false, false, true, false, false, true))))));
    t.checkExpect(this.worldTester1.hasPath(this.worldTester1.board.get(1).get(1)), true);
    t.checkExpect(this.worldTester1.hasPath(this.worldTester1.board.get(0).get(0)), true);
    this.worldTester1.randRotateBoard();
    t.checkExpect(this.worldTester1.hasPath(this.worldTester1.board.get(1).get(1)), false);
  }

  //testing the onMouseClicked method
  void testOnMouseClicked(Tester t) {
    this.initData();
    this.worldTester1.initBoard();
    this.worldTester1.buildMST(this.worldTester1.board);
    this.worldTester1.visualiseConnections();
    this.worldTester1.hasPath(this.worldTester1.board.get(0).get(0));
    //win
    t.checkExpect(this.worldTester1.board.get(0).get(0), 
        new GamePiece(0, 0, false, true, false, true, true, true));
    this.worldTester1.onMouseClicked(new Posn(1, 1), "LeftButton");
    t.checkExpect(this.worldTester1.board.get(0).get(0), 
        new GamePiece(0, 0, false, true, false, true, true, true));

    this.initData();
    this.worldTester1.initBoard();
    this.worldTester1.buildMST(this.worldTester1.board);
    this.worldTester1.visualiseConnections();
    this.worldTester1.randRotateBoard();
    //in progress
    t.checkExpect(this.worldTester1.board.get(0).get(1), 
        new GamePiece(1, 0, true, true, false, false, false, false));
    this.worldTester1.onMouseClicked(new Posn(31, 1), "LeftButton");
    t.checkExpect(this.worldTester1.board.get(0).get(1), 
        new GamePiece(1, 0, false, false, true, true, false, false));
    this.worldTester1.onMouseClicked(new Posn(31, 1), "LeftButton");
    t.checkExpect(this.worldTester1.board.get(0).get(1), 
        new GamePiece(1, 0, true, true, false, false, false, false));
    this.worldTester1.onMouseClicked(new Posn(1, 1), "LeftButton");
    this.worldTester1.onMouseClicked(new Posn(1, 1), "LeftButton");
    t.checkExpect(this.worldTester1.board.get(0).get(1), 
        new GamePiece(1, 0, true, true, false, false, false, true));
    //right button
    this.worldTester1.onMouseClicked(new Posn(31, 1), "RightButton");
    t.checkExpect(this.worldTester1.board.get(0).get(1), 
        new GamePiece(1, 0, true, true, false, false, false, true));
  }

  //testing the makeScene method
  void testMakeScene(Tester t) {
    this.initData();
    this.worldTester1.initBoard();
    this.worldTester1.buildMST(this.worldTester1.board);
    this.worldTester1.visualiseConnections();
    this.worldTester1.hasPath(this.worldTester1.board.get(0).get(0));
    WorldScene scene1 = this.worldTester1.makeScene(); 
    //Win condition
    WorldScene expectedScene1 = new WorldScene(3 * 30, 3 * 30);
    WorldImage columnImage1 = new EmptyImage();
    for (int c = 0; c < 3; c++) {
      WorldImage rowImage1 = new EmptyImage();
      for (int r = 0; r < 3; r++) {
        GamePiece current1 = this.worldTester1.board.get(c).get(r);
        WorldImage expectedImage1 = 
            current1.tileImage(30, 3, current1.wireColor(), (r == 0 && c == 0));
        rowImage1 = new BesideImage(rowImage1, expectedImage1);
      }
      columnImage1 = new AboveImage(columnImage1, rowImage1);
    }
    expectedScene1.placeImageXY(columnImage1, 45, 45);
    expectedScene1.placeImageXY(new TextImage("You Win, Good Job!", 4, Color.WHITE), 45, 45);
    t.checkExpect(scene1, expectedScene1);

    this.initData();
    this.worldTester1.initBoard();
    this.worldTester1.buildMST(this.worldTester1.board);
    this.worldTester1.visualiseConnections();
    this.worldTester1.randRotateBoard();
    WorldScene scene = this.worldTester1.makeScene();   
    //Still running
    WorldScene expectedScene = new WorldScene(3 * 30, 3 * 30);
    WorldImage columnImage = new EmptyImage();
    for (int c = 0; c < 3; c++) {
      WorldImage rowImage = new EmptyImage();
      for (int r = 0; r < 3; r++) {
        GamePiece current = this.worldTester1.board.get(c).get(r);
        WorldImage expectedImage = 
            current.tileImage(30, 3, current.wireColor(), (r == 0 && c == 0));
        rowImage = new BesideImage(rowImage, expectedImage);
      }
      columnImage = new AboveImage(columnImage, rowImage);
    }
    expectedScene.placeImageXY(columnImage, 45, 45);
    t.checkExpect(scene, expectedScene);

  }

  // test for onKeyEvent
  void testOnKeyEvent(Tester t) {
    initData();
    this.worldTester1.initBoard();
    this.worldTester1.buildMST(this.worldTester1.board);
    this.worldTester1.visualiseConnections();
    this.worldTester1.board.get(2).get(2).rotate(1);
    this.worldTester1.onKeyEvent("left");
    t.checkExpect(this.worldTester1.powerRow, 0);
    this.worldTester1.onKeyEvent("right");
    t.checkExpect(this.worldTester1.powerRow, 1);
    this.worldTester1.onKeyEvent("left");
    t.checkExpect(this.worldTester1.powerRow, 0);
    this.worldTester1.onKeyEvent("up");
    t.checkExpect(this.worldTester1.powerCol, 0);
    this.worldTester1.onKeyEvent("down");
    t.checkExpect(this.worldTester1.powerCol, 1);
    this.worldTester1.onKeyEvent("up");
    t.checkExpect(this.worldTester1.powerCol, 0);
    this.worldTester1.onKeyEvent("a");
    t.checkExpect(this.worldTester1.powerRow, 0);
    t.checkExpect(this.worldTester1.powerCol, 0); 
    this.worldTester1.board.get(2).get(2).rotate(3);
    this.worldTester1.onKeyEvent("right");
    t.checkExpect(this.worldTester1.powerRow, 0);
  }

  //test for the method createMSTWorklist
  void testCreateMSTWorkList(Tester t) {
    initData();
    this.worldTester2.initBoard();
    GamePiece gp1 = this.worldTester2.board.get(0).get(0);
    GamePiece gp2 = this.worldTester2.board.get(0).get(1);
    GamePiece gp3 = this.worldTester2.board.get(1).get(0);
    GamePiece gp4 = this.worldTester2.board.get(1).get(1);
    t.checkExpect(this.worldTester2.mst, new ArrayList<Edge>());
    t.checkExpect(this.worldTester2.createMSTWorklist(this.worldTester2.board), 
        new ArrayList<Edge>(Arrays.asList(
            new Edge(gp3, gp4, 0), 
            new Edge(gp4, gp3, 6), 
            new Edge(gp1, gp2, 8), 
            new Edge(gp2, gp4, 17), 
            new Edge(gp4, gp2, 19), 
            new Edge(gp1, gp3, 22), 
            new Edge(gp3, gp1, 39), 
            new Edge(gp2, gp1, 40))));
  }
  
  //test for the method find
  void testFind(Tester t) {
    this.initData();
    this.worldTester1.initBoard();
    HashMap<GamePiece, GamePiece> representatives = new HashMap<GamePiece, GamePiece>();   
    for (GamePiece gp : this.worldTester1.nodes) {
      representatives.put(gp, gp);
    }   
    
    GamePiece node1 = this.worldTester1.board.get(0).get(0);
    t.checkExpect(this.worldTester1.find(representatives, node1), node1);  
    GamePiece node2 = this.worldTester1.board.get(1).get(1);
    representatives.put(node2, node1);
    t.checkExpect(this.worldTester1.find(representatives, node2), node1);
  }
  
  //test for the method buildMST
  void testBuildMST(Tester t) {
    this.initData();
    this.worldTester2.initBoard();
    GamePiece gp1 = this.worldTester2.board.get(0).get(0);
    GamePiece gp2 = this.worldTester2.board.get(0).get(1);
    GamePiece gp3 = this.worldTester2.board.get(1).get(0);
    GamePiece gp4 = this.worldTester2.board.get(1).get(1);
    t.checkExpect(this.worldTester2.mst.size(), 0);
    t.checkExpect(this.worldTester2.mst, 
        new ArrayList<Edge>());
    this.worldTester2.buildMST(this.worldTester2.board);
    t.checkExpect(this.worldTester2.mst.size(), 3);
    t.checkExpect(this.worldTester2.mst, 
        new ArrayList<Edge>(
            Arrays.asList(
                new Edge(gp3, gp4, 0),
                new Edge(gp1, gp2, 8), 
                new Edge(gp2, gp4, 17))));
  }
  
  //test for the method visualiseConnections
  void testVisualiseConnections(Tester t) {
    this.initData();
    this.worldTester2.initBoard();
    this.worldTester2.buildMST(this.worldTester2.board);
    GamePiece gp1 = this.worldTester2.board.get(0).get(0);
    GamePiece gp2 = this.worldTester2.board.get(0).get(1);
    GamePiece gp3 = this.worldTester2.board.get(1).get(0);
    GamePiece gp4 = this.worldTester2.board.get(1).get(1);
    t.checkExpect(gp1, new GamePiece(0, 0, false, false, false, false, true, false));
    t.checkExpect(gp2, new GamePiece(1, 0, false, false, false, false, false, false));
    t.checkExpect(gp3, new GamePiece(0, 1, false, false, false, false, false, false));
    t.checkExpect(gp4, new GamePiece(1, 1, false, false, false, false, false, false));
    this.worldTester2.visualiseConnections();
    gp1 = this.worldTester2.board.get(0).get(0);
    gp2 = this.worldTester2.board.get(0).get(1);
    gp3 = this.worldTester2.board.get(1).get(0);
    gp4 = this.worldTester2.board.get(1).get(1);
    t.checkExpect(gp1, new GamePiece(0, 0, false, true, false, false, true, false));
    t.checkExpect(gp2, new GamePiece(1, 0, true, false, false, true, false, false));
    t.checkExpect(gp3, new GamePiece(0, 1, false, true, false, false, false, false));
    t.checkExpect(gp4, new GamePiece(1, 1, true, false, true, false, false, false));
  }
}