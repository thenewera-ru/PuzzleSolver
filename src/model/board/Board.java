package model.board;

import java.util.*;

import utils.Utils;
import measure.Measure;

public class Board {

    private class Node {
        public int x, y;
        public Node(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }


    private int n;
    private int[][] instance;

    private int stepsToSolve;

    private Node blankNode;

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
        blankNode = from.blankNode;
    }

    public int getValue(int x, int y) {
        return this.instance[x][y];
    }


    public void move(Node tile) {
        instance[blankNode.x][blankNode.y] = instance[tile.x][tile.y];
        instance[tile.x][tile.y] = 0;
        blankNode = tile;
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
                if (dx * dy != 0 || (dx == 0 && dy == 0))
                    continue;
                Node to = new Node(blankNode.x + dx, blankNode.y + dy);
                moves.add(to);
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

    public List<Board> getAvailableBoards() {
        List<Board> boards = new ArrayList<>(4);
        for (Node move : getAvailableMoves())
            boards.add(stepForward(move));
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
        int shuffles = 30; // [30, 100]
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
                error += m.call(x - xy[0], y - xy[1]);
            }
        }
        return error;
    }

    private List<Board> getSnapshots(Map<Board, Board> graph, Board current) {
        // Return the consecutive boards : each board represent the state of the game.
        Stack<Board> stck = new Stack<>();
        while (current != null) {
            stck.push(current);
            current = graph.get(current);
        }
        List<Board> ans = new ArrayList<>();
        while (!stck.isEmpty())
            ans.add(stck.pop());
        return ans;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Board) {
            for (int i = 0; i < n; ++i) {
                for (int j = 0; j < n; ++j) {
                    if (instance[i][j] != ((Board)o).getValue(i, j))
                        return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                hash += ((n * n * hash) + instance[i][j]);
            }
        }
        return hash;
    }


    public List<Board> solveUsingSmartDijkstra(Measure m) {
        HashMap<Board, Board> graph = new HashMap<>(); // To restore the path
        HashMap<Board, Integer> depth = new HashMap<>(); // How "deep" the search has gone
        final HashMap<Board, Integer> score = new HashMap<>(); // F values of each available move
        Comparator<Board> cmp = new Comparator<Board>() {
            @Override
            public int compare(Board o1, Board o2) {
                return score.get(o1) - score.get(o2);
            }
        };
        Queue<Board> open = new PriorityQueue<>(cmp);
        graph.put(this, null);
        depth.put(this, 0);
        open.add(this);
        while (open.size() > 0) {
            // stepsToSolve += 1; // stepForward()

            Board bestMove = open.remove();
            if (bestMove.isSolved())
                return getSnapshots(graph, bestMove);
            for (Board nextState: bestMove.getAvailableBoards()) {
                if (graph.containsKey(nextState))
                    continue;
                graph.put(nextState, bestMove); // Update parent-child relationship
                depth.put(nextState, depth.get(bestMove) + 1); // Update "how deep" traversal has gone
                int error = nextState.error(m); // H (heuristic) score.
                score.put(nextState, depth.get(bestMove) + error);
                // First of all we update score for the node. F = G + H.
                // (1) updateScore(@nextState)
                // (2) addToQueue(@nextState)
                open.add(nextState);
            }
        }
        // We haven't reached the final state. So returning null
        return null;
    }

    public int getPathLength() {
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
