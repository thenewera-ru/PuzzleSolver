package io.writer;

import java.io.OutputStream;
import java.io.PrintWriter;

public class OutputWriter {
    private PrintWriter out;

    public OutputWriter(OutputStream os) {
        this.out = new PrintWriter(os);
    }

    public PrintWriter getInstance() { return this.out; }

    public void close() {
        this.out.close();
    }
}
