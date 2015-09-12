package org.dolphin.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * Created by hanyanan on 2015/5/12.
 */
public class InputStreamWrapper extends InputStream{
    private final InputStream inputStream;
    private final HttpURLConnection connection;
    private long readCount = 0;
    public InputStreamWrapper(InputStream inputStream, HttpURLConnection connection) {
        this.inputStream = inputStream;
        this.connection = connection;
    }
    @Override
    public int read() throws IOException {
        int ch = inputStream.read();
        if(ch != -1){
            ++readCount;
            onRead(readCount);
        }
        return ch;
    }

    public int read(byte b[]) throws IOException {
        int count = inputStream.read(b);
        if(count > 0){
            readCount += count;
            onRead(readCount);
        }
        return count;
    }

    public int read(byte b[], int off, int len) throws IOException {
        int count = inputStream.read(b, off, len);
        if(count > 0){
            readCount += count;
            onRead(readCount);
        }
        return count;
    }

    public long skip(long n) throws IOException {
        long skip = inputStream.skip(n);
        if(skip > 0){
            readCount += skip;
            onRead(readCount);
        }
        return skip;
    }

    public int available() throws IOException {
        return inputStream.available();
    }

    public void close() throws IOException {
        inputStream.close();
        connection.disconnect();
    }
    public synchronized void mark(int readlimit) {
        inputStream.mark(readlimit);
    }
    public synchronized void reset() throws IOException {
        inputStream.reset();
    }
    public boolean markSupported() {
        return inputStream.markSupported();
    }

    protected void onRead(long readCount) throws IOException{
        
    }
}
