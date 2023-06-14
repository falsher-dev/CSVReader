package ru.falsher.io;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class CSVReader {
    boolean needToNextLine = false;

    private boolean end_of_input = false;

    private InputStreamReader in;

    private StringBuilder sb = new StringBuilder();

    private boolean isClosed = false;

    public CSVReader(InputStream in) {
        this.in = new InputStreamReader(in, StandardCharsets.UTF_8);
        //int code = (((in.read() << 8) | in.read()) << 8) | in.read();
    }

    public CSVReader(String filename) throws FileNotFoundException {
        this(new FileInputStream(filename));
    }

    public String next() throws IOException {
        if (isClosed) return null;
        sb.setLength(0);
        if (needToNextLine) return null;
        int b;
        boolean string_reading=false;
        boolean postIsCov = false;
        boolean end_of_input = false;
        while ((b = in.read()) != -1) {
            if (b == 13) continue;
            if (end_of_input && b == ' ') continue;
            if (b == 10) {
                needToNextLine = true;
                if (sb.length() == 0) return null;
                return sb.toString();
            }
            if (b == '"') {
                if (!string_reading) {
                    string_reading = true;
                    continue;
                }
                if (postIsCov) {
                    sb.append('"');
                    postIsCov = false;
                }
                else {
                    postIsCov = true;
                    continue;
                }
            } else if (postIsCov){
                string_reading = false;
                end_of_input = true;
            }
            if (end_of_input) {
                if (b == ';') {
                    if (sb.length() == 0) return null;
                    return sb.toString();
                }
            } else if (b == ';'){
                if (string_reading) {
                    sb.append(';');
                    continue;
                }
                else {
                    if (sb.length() == 0) return null;
                    return sb.toString();
                }
            }
            if (!end_of_input) sb.appendCodePoint(b);
        }
        this.end_of_input = true;
        if (sb.length() == 0) return null;
        return sb.toString();
    }

    public void skip(int count) throws IOException {
        for (;count > 0; count--) skip();
    }

    public void skip() throws IOException {
        if (isClosed) return;
        if (needToNextLine) return;
        int b;
        boolean string_reading=false;
        boolean postIsCov = false;
        boolean end_of_input = false;
        while ((b = in.read()) != -1) {
            if (b == 13) continue;
            if (end_of_input && b == ' ') continue;
            if (b == 10) {
                needToNextLine = true;
                return;
            }
            if (b == '"') {
                if (!string_reading) {
                    string_reading = true;
                    continue;
                }
                if (postIsCov) {
                    postIsCov = false;
                }
                else {
                    postIsCov = true;
                    continue;
                }
            } else if (postIsCov){
                string_reading = false;
                end_of_input = true;
            }
            if (end_of_input) {
                if (b == ';') return;
            } else if (b == ';'){
                if (!string_reading) return;
            }
        }
        this.end_of_input = true;
    }

    public boolean nextLine() throws IOException {
        if (isClosed) return true;
        if (!needToNextLine){
            int c;
            while ((c = in.read()) != -1 && c != '\n');
        }
        else needToNextLine = false;
        return end_of_input;
    }

    public void close() throws IOException {
        isClosed = true;
        in.close();
    }
}
