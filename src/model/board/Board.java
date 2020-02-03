package model.board;

import java.util.*;

import utils.Utils;
import measure.Measure;

public class Board implements Comparable<Board> {

    private class Node {
        public int x, y;
        public Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void copyFrom(Node that) {
            this.x = that.x;
            this.y = that.y;
        }
    }


    private int n;
    private int[][] instance;

    private int stepsToSolve;

    private Node blankNode;

    private int score;

    public Board(int n) {
        this.n = n;
        instance = new int[n][n];
        int cnt = 1;
        for (int x = 0; x < n; ++x) {
            for (int y = 0; y < n; ++y) {
                instance[x][y] = cnt++;
            }
        }
        blankNode = new Node(n - 1, n - 1);
        instance[n - 1][n - 1] = 0;
        stepsToSolve = 0;
    }

    public Board(Board from) {
        this(from.getSize());
        for (int x = 0; x < n; ++x)
            for (int y = 0; y < n; ++y)
                instance[x][y] = from.getValue(x, y);
        stepsToSolve = from.stepsToSolve;
        blankNode.copyFrom(from.blankNode);
    }

    public int getValue(int x, int y) {
        return this.instance[x][y];
    }



    public int getSize() {
        return this.n;
    }

    public List<Node> getAvailableMoves() {
        List<Node> moves = new ArrayList<>(4);
        for (int dx = -1; dx < 2; ++dx) {
            for (int dy = -1; dy < 2; ++dy) {
                int tox = blankNode.x + dx;
                int toy = blankNode.y + dy;
                // Check boundaries
                if (tox < 0 || tox >= n || toy < 0 || toy >= n)
                    continue;
                //
                if (dx * dy != 0 || (dx + dy == 0))
                    continue;
                Node safeMove = new Node(tox, toy);
                moves.add(safeMove);
            }
        }
        return moves;
    }

    private Board stepForward(Node move) {
        Board ans = new Board(this);
        ans.move(move);
        ans.stepsToSolve += 1;
        return ans;
    }

    public void move(Node newBlankTile) {
        instance[blankNode.x][blankNode.y] = instance[newBlankTile.x][newBlankTile.y];
        instance[newBlankTile.x][newBlankTile.y] = 0;
        blankNode.copyFrom(newBlankTile);
    }

    public List<Board> getAvailableBoards() {
        List<Board> boards = new ArrayList<>(4);
        for (Node move : getAvailableMoves())
            boards.add(this.stepForward(move));
        return boards;
    }


    public void shuffle(int times) {
        while (times-- > 0) {
            List<Node> possibleMoves = getAvailableMoves();
            int pos = (int)(Math.random() * possibleMoves.size());
            this.move(possibleMoves.get(pos));
        }
    }

    public void shuffle() {
        int shuffles = 30;
        this.shuffle(shuffles);
    }

    public boolean isSolved() {
        int cnt = 0; // How many tiles are misplaced
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                int correctValue = n * i + j + 1;
                if (instance[i][j] > 0 && instance[i][j] != correctValue)
                    cnt += 1;
            }
        }
        return cnt == 0;
    }

    public int error(Measure m) {
        // @m - any function you specify: f = f(x,y). E.g. Manhattan distance, Euclidean distance, etc..
        int error = 0;
        for (int x = 0; x < n; ++x) {
            for (int y = 0; y < n; ++y) {
                if (instance[x][y] == 0)
                    continue;
                int[] xy = Utils.getPositionFromMatrix(instance[x][y] - 1, n, n);
                error += m.call(Math.abs(x - xy[0]), Math.abs(y - xy[1]));
            }
        }
        return error;
    }

    @Override
    public Board clone() {
        return new Board(this);
    }

    private List<Board> getSnapshots(Map<Board, Board> parent, Board current) {
        // Return the consecutive boards : each board represent the state of the game.
        Stack<Board> stck = new Stack<>();
        while (current != null) {
            stck.push(current);
            current = parent.get(current);
        }
        List<Board> ans = new ArrayList<>();
        while (!stck.isEmpty())
            ans.add(stck.pop());
        return ans;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Board) {
            Board other = (Board)o;
            for (int i = 0; i < n; ++i) {
                for (int j = 0; j < n; ++j) {
                    if (instance[i][j] != other.getValue(i, j))
                        return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return java.util.Arrays.deepHashCode(instance);
    }

    @Override
    public int compareTo(Board that) {
        return this.hashCode() - that.hashCode();
    }


    public List<Board> solveUsingSmartDijkstra(Measure m) {
        HashMap<Board, Board> parent = new HashMap<>(); // To restore the path
        HashMap<Board, Integer> depth = new HashMap<>(); // How "deep" the search has gone
        HashMap<Board, Integer> score = new HashMap<>(); // F values of each available move
        Comparator<Board> cmp = new Comparator<Board>() {
            @Override
            public int compare(Board o1, Board o2) {
                return score.get(o1) - score.get(o2);
            }
        };
        PriorityQueue<Board> open = new PriorityQueue<>(cmp);
        HashSet<Board> explored = new HashSet<>();
        parent.put(this, null);
        depth.put(this, 0);
        open.add(this);
        score.put(this, this.error(m));
        while (open.size() > 0) {
            // stepsToSolve += 1; // stepForward()
            Board bestCurrentState = open.remove();
            if (bestCurrentState.isSolved())
                return getSnapshots(parent, bestCurrentState);
            for (Board nextState: bestCurrentState.getAvailableBoards()) {
                if (explored.contains(nextState))
                    continue;
                if (!open.contains(nextState)) {
                    Board to = nextState.clone();
                    Board from = bestCurrentState.clone();
                    parent.put(to, from); // Update parent-child relationship
                    int curDepth = depth.get(from);
                    depth.put(to, curDepth + 1); // Update "how deep" traversal has gone
                    int error = to.error(m); // H (heuristic) score.
                    to.score = curDepth + 1 + error;
                    score.put(to, to.score);
                    // First of all we update score for the node. F = G + H.
                    // (1) updateScore(@nextState)
                    // (2) addToQueue(@nextState)
                    open.add(to);
                } else if (open.contains(nextState)) { // We have already seen the bestMove state. Maybe we have found better, huh?
                    // Sort of like relaxation (aka relaxing bounds) in Dijkstra search
                    int visitedMoveValue = score.get(nextState);
                    int bestMoveValue = depth.get(bestCurrentState) + 1 + nextState.error(m);
                    if (bestMoveValue < visitedMoveValue) { // Our path is better, so update correctly
                        Board to = nextState.clone();
                        Board from = bestCurrentState.clone();
                        parent.put(to, from);
                        depth.put(to, depth.get(from) + 1);
                        open.remove(to);
                        to.score = bestMoveValue;
                        score.put(to, nextState.score);
                        open.add(to);
                    }
                }
            }
            explored.add(bestCurrentState);
        }
        // We haven't reached the final state. So returning null
        return null;
    }

    public final int getPathLength() {
        return this.stepsToSolve;
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder();
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                ans.append(Integer.toString(instance[i][j]));
                if (j < n - 1)
                    ans.append(" ");
                else
                    ans.append("\n");
            }
        }
        return ans.toString();
    }

}
