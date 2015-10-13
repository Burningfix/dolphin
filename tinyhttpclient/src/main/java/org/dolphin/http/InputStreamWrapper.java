package org.dolphin.http;

import org.dolphin.lib.IOUtil;
import org.dolphin.lib.exception.AbortException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * Created by hanyanan on 2015/5/12.
 */
class InputStreamWrapper extends InputStream {
    private final InputStream inputStream;
    private final HttpURLConnection connection;
    private long readCount = 0;

    InputStreamWrapper(InputStream inputStream, HttpURLConnection connection) {
        this.inputStream = inputStream;
        this.connection = connection;
    }

    @Override
    public int read() throws IOException {
        try {
            int ch = inputStream.read();
            if (ch != -1) {
                ++readCount;
                onRead(readCount);
            }
            return ch;
        } catch (AbortException e) {
            IOUtil.closeQuietly(this);
            throw e;
        } catch (IOException e) {
            IOUtil.closeQuietly(this);
            throw e;
        }
    }

    public int read(byte b[]) throws IOException {
        try {
            int count = inputStream.read(b);
            if (count > 0) {
                readCount += count;
                onRead(readCount);
            }
            return count;
        } catch (AbortException e) {
            IOUtil.closeQuietly(this);
            throw e;
        } catch (IOException e) {
            IOUtil.closeQuietly(this);
            throw e;
        }
    }

    public int read(byte b[], int off, int len) throws IOException {
        try {
            int count = inputStream.read(b, off, len);
            if (count > 0) {
                readCount += count;
                onRead(readCount);
            }
            return count;
        } catch (AbortException e) {
            IOUtil.closeQuietly(this);
            throw e;
        } catch (IOException e) {
            IOUtil.closeQuietly(this);
            throw e;
        }
    }

    public long skip(long n) throws IOException {
        try {
            long skip = inputStream.skip(n);
            if (skip > 0) {
                readCount += skip;
                onRead(readCount);
            }
            return skip;
        } catch (AbortException e) {
            IOUtil.closeQuietly(this);
            throw e;
        } catch (IOException e) {
            IOUtil.closeQuietly(this);
            throw e;
        }
    }

    public int available() throws IOException {
        try {
            return inputStream.available();
        } catch (AbortException e) {
            IOUtil.closeQuietly(this);
            throw e;
        } catch (IOException e) {
            IOUtil.closeQuietly(this);
            throw e;
        }
    }

    public void close() throws IOException {
        IOUtil.closeQuietly(inputStream);
        connection.disconnect();
    }

    @Override
    public final void finalize() throws Throwable {
        connection.disconnect();
        super.finalize();
    }

    public synchronized void mark(int readlimit) {
        inputStream.mark(readlimit);
    }

    public synchronized void reset() throws IOException {
        try {
            inputStream.reset();
        } catch (AbortException e) {
            IOUtil.closeQuietly(this);
            throw e;
        } catch (IOException e) {
            IOUtil.closeQuietly(this);
            throw e;
        }
    }

    public boolean markSupported() {
        return inputStream.markSupported();
    }

    protected void onRead(long readCount) throws IOException, AbortException {

    }
}
