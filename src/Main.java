import measure.Measure;
import model.Model;
import model.board.Board;
import io.writer.OutputWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Main {

    static final int n = 5; // Size of the board.
    static final int trials = 1; // Experiments

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
        Map<Pair, Integer> results = new TreeMap<>();
        Map<Pair, Model> compiledModels = compile(10, 5, 30);
        ExecutorService pool = Executors.newFixedThreadPool(10);
        for (Pair key : compiledModels.keySet()) {
            Model m = compiledModels.get(key);
            pool.execute(m);
        }
        pool.shutdown();

        try {
            save(results, "output.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Map<Pair, Model> compile(int experiments, int size, int randomMoves) {
        Map<Pair, Model> ans = new TreeMap<>();
        Random r = new Random();
        for (int i = 0; i < experiments; ++i) {
            int a = 1 + r.nextInt(10), b = 1 + r.nextInt(10);
            if (ans.containsKey(new Pair(a, b)))
                continue;
            Model m = new Model();
            Measure metric = (int x, int y) -> { return a * x + b * y; };
            m.setMeasure(metric);
            m.setSize(size);
            m.setRandomMoves(randomMoves);
            m.compile();
            ans.put(new Pair(a, b), m);
        }
        return ans;
    }


    static void save(Map<Pair, Integer> data, String fileName) throws IOException {
        String path = new File(".").getCanonicalPath();
        OutputStream os = new FileOutputStream(path + "/" + fileName);
        OutputWriter writer = new OutputWriter(os);
        writer.getInstance().println("a\tb\tMoves");
        for (Pair key : data.keySet()) {
            writer.getInstance().println(key.first + "\t" + key.second + "\t" + data.get(key));
        }
        writer.close();
    }
}
