package ru.falsher.io;

import java.io.*;
import java.util.function.BiFunction;

public class CSVGenerator {
    private PrintStream printWriter;

    private CSVReader reader;
    private int[] columns;
    private BiFunction<Integer,String,String> func;

    boolean isRewritingMode = false;

    boolean secondElement = false;

    private boolean isClosed = false;

    public CSVGenerator(OutputStream os, boolean i) throws IOException {
        if (i) os.write(new byte[]{(byte)0xEF,(byte)0xBB,(byte)0xBF});
        printWriter = new PrintStream(os,true,"UTF-8");
    }
    public CSVGenerator(String filename, boolean i) throws IOException {
        this(new FileOutputStream(filename,!i),i);
    }

    //BiFunction: first parameter - column id, second parameter - column number, return value - nev column text
    public CSVGenerator(CSVReader reader, int[] columns, BiFunction<Integer,String,String> func, OutputStream os, boolean i) throws IOException {
        if (i) os.write(new byte[]{(byte)0xEF,(byte)0xBB,(byte)0xBF});
        printWriter = new PrintStream(os,true,"UTF-8");
        this.reader = reader;
        this.columns = columns;
        this.func = func;
        isRewritingMode = true;
    }

    public void nextLine() {
        if (isClosed) return;
        secondElement = false;
        printWriter.println();
    }

    public void add(String content, boolean putKov){
        add(content != null && (content.contains(";") || content.contains("\"") || putKov), content);
    }

    private void add(boolean putKov, String content) {
        if (isClosed) return;
        if (secondElement)
            printWriter.print(';');
        else secondElement = true;
        if (content == null) return;
        if (putKov){
            content = content.replace("\"","\"\"");
            printWriter.print('\"');
            printWriter.print(content);
            printWriter.print('\"');
        } else printWriter.print(content);
    }

    public void add(String content) {
        add(content != null && (content.contains(";") || content.contains("\"")), content);
    }

    public void next() {
        if (isClosed) return;
        if (isRewritingMode){
            try{
                int i=0;
                int min = -1; int max = 0;
                for (int o: columns) {
                    if (min == -1) min = o;
                    if (min > o) min = o;
                    if (max < o) max = o;
                }
                String column;
                while (true){
                    column = reader.next();
                    if (reader.needToNextLine) {
                        if (reader.nextLine()) break;
                        nextLine();
                        i = 0;
                    }
                    else {
                        if (i >= min && i <= max) {
                            for (int o: columns) if (i == o) add(func.apply(i,column));
                        }
                        else add(column);
                        i++;
                    }
                }
            }catch (IOException e){
                e.printStackTrace();
            }
            return;
        }
        printWriter.print(';');
    }

    public void close() throws IOException {
        isClosed = true;
        if (isRewritingMode) reader.close();
        printWriter.close();
    }
}
