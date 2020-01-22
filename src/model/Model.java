package model;

import measure.Measure;
import model.board.Board;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class Model implements Runnable {

    private class Node {
        public int x, y;
        public Node(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private Measure h; // Heuristic function
    private int size; // The size of the grid
    private int shuffleMoves; // How many random moves from the correct solution

    private Board board;

    private List<Board> solution;

    private long timeToFindSolution;

    public Model() {
        this.h = null;
        this.size = 4;
        this.shuffleMoves = 30;
    }


    public Model setMeasure(Measure m) {
        this.h = m;
        return this;
    }

    public Model setRandomMoves(int shuffleMoves) {
        this.shuffleMoves = shuffleMoves;
        return this;
    }

    public Model setSize(int size) {
        this.size = size;
        return this;
    }

    public Model compile() {
        this.board = new Board(this.size);
        this.board.shuffle(this.shuffleMoves);
        return this;
    }

    @Override
    public void run() {
        Instant start = Instant.now();
        this.solution = this.board.solveUsingSmartDijkstra(this.h);
        Instant end = Instant.now();
        this.timeToFindSolution = Duration.between(start, end).toNanos();
    }

    public List<Board> getSolution() {
        return this.solution;
    }

    final public int getPathLength() {
        if (this.solution == null)
            return 0;
        return this.solution.get(solution.size() - 1).getPathLength();
    }

    final public long getTimeToFindSolution() {
        return this.timeToFindSolution;
    }
}
