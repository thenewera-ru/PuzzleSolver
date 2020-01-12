package io.reader;


import java.io.IOException;
import java.io.InputStream;
import java.util.InputMismatchException;

class InputReader {

    class EOF extends Exception {
        public EOF(String msg) {
            super(msg);
        }
    }

    private InputStream stream;
    private byte[] buf;
    private int curChar = 0, snumChars = 0;

    private SpaceCharFilter filter;

    public InputReader(InputStream stream) {
        this.stream = stream;
        this.buf = new byte[8192];
        this.filter = null;
    }

    public void setSpaceCharFilter(SpaceCharFilter filter) {
        this.filter = filter;
    }


    public int snext() {
        if (curChar >= snumChars) {
            curChar = 0;
            try {
                snumChars = stream.read(buf);
            } catch (IOException e) {
                throw new InputMismatchException();
            }
            if (snumChars <= 0) {
                return -1;
            }
        }
        return buf[curChar++];
    }

    public int nextInt() throws EOF, InputMismatchException {
        int c = snext();
        while (isSpaceChar(c) && c != -1) {
            c = snext();
        }
        if (c == -1) {
            throw new EOF("End of file");
        }
        int sgn = 1;
        if (c == '-') {
            sgn = -1;
            c = snext();
        }
        int res = 0;
        while (!isSpaceChar(c)) {
            if (c < '0' || c > '9') {
                throw new InputMismatchException();
            }
            res *= 10;
            res += c - '0';
            c = snext();
        }
        return res * sgn;
    }

    public double nextDouble() throws EOF, InputMismatchException {
        return Double.parseDouble(nextString());
    }

    public long nextLong() throws EOF, InputMismatchException {
        return Long.parseLong(nextString());
    }

    public String nextString() throws InputMismatchException, EOF {
        int c = snext();
        while (isSpaceChar(c) && c != -1) {
            c = snext();
        }
        if (c == -1) {
            throw new EOF("End of file");
        }
        StringBuilder res = new StringBuilder();
        do {
            res.appendCodePoint(c);
            c = snext();
        } while (!isSpaceChar(c));
        return res.toString();
    }



    public boolean isSpaceChar(int c) {
        if (filter != null) {
            return filter.isSpaceChar(c);
        }
        return c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == -1;
    }

    public interface SpaceCharFilter {
        public boolean isSpaceChar(int ch);
    }
}

