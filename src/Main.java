import measure.Measure;
import model.Model;
import model.board.Board;
import io.writer.OutputWriter;
import utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main {

    static class Pair implements Comparable<Pair>{
        int first, second;
        public Pair(int first, int second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public int compareTo(Pair o) {
            if (first < o.first) {
                return -1;
            } else if (first > o.first) {
                return 1;
            } else {
                if (second == o.second)
                    return 0;
                return second < o.second ? -1 : 1;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Map<Pair, Model> models = compile(1, 4, 50);
        Map<Pair, long[]> results = new TreeMap<>();
        for (Pair key : models.keySet()) {
            Model m = models.get(key);
            m.run();
            long[] ans = {m.getPathLength(), m.getTimeToFindSolution()};
            results.put(key, ans);
        }
        save(results, "output.txt");
//        int[][] arr1 = {
//                {1, 2, 3},
//                {4, 5, 6},
//                {7, 8, 9}
//        };
//        int[][] arr2 = {
//                {1, 2, 3},
//                {4, 5, 6},
//                {7, 8, 9}
//        };
    }

    static Map<Pair, Model> compile(int experiments, int size, int randomMoves) {
        Map<Pair, Model> ans = new TreeMap<>();
        Measure manhattanDistance = (int x, int y) -> { return x + y; };
        Measure euclideanDistance = (int x, int y) -> { return x^2 + y^2; };
        for (int i = 0; i < experiments; ++i) {
            Model m1 = new Model();
            m1.setMeasure(euclideanDistance);
            m1.setSize(size);
            m1.setRandomMoves(randomMoves);
            m1.compile();
            ans.put(new Pair((i + 1), 1), m1);
            Model m2 = new Model();
            m2.setMeasure(manhattanDistance);
            m2.setSize(size);
            m2.setRandomMoves(randomMoves);
            m2.compile();
            ans.put(new Pair((i + 1), 2), m2);
        }
        return ans;
    }


    static void save(Map<Pair, long[]> data, String fileName) throws IOException {
        String path = new File(".").getCanonicalPath();
        OutputStream os = new FileOutputStream(path + "/" + fileName);
        OutputWriter writer = new OutputWriter(os);
        writer.printLine("Experiment\tMetric\t\t\tMoves\t\tTime(nanoseconds)");
        for (Pair key : data.keySet()) {
            writer.printLine(key.first + "\t\t\t" + (key.second == 1 ? "Manhattan" : "Euclidean") + "\t\t\t" +
                    data.get(key)[0] + "\t\t" + data.get(key)[1]);
        }
        writer.close();
    }
}
