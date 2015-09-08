package org.dolphin.lib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;

import static org.dolphin.lib.Preconditions.checkArgument;
import static org.dolphin.lib.Preconditions.checkNotNull;

/**
 * Created by hanyanan on 2015/3/6.
 */
public class IOUtil {
    public static final int M = 1024 * 1024;
    public static final int K = 1024;
    public static final int DEFAULT_BUFF_SIZE = 8 * K;
    public static final int DEFAULT_DISK_SIZE = 20 * M;


    // delete methods
    public static void safeDeleteIfExists(File file) {
        try {
            deleteIfExists(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteIfExists(File file) throws IOException {
        if (file.exists() && !file.delete()) {
            throw new IOException("delete failed");
        }
    }

    /**
     * Deletes the contents of {@code dir}. Throws an IOException if any file
     * could not be deleted, or if {@code dir} is not a readable directory.
     */
    public static void deleteDirectory(File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) {
            throw new IOException("not a readable directory: " + dir);
        }
        for (File file : files) {
            if (file.isDirectory()) {
                deleteDirectory(file);
            }
            if (!file.delete()) {
                throw new IOException("failed to delete file: " + file);
            }
        }
    }

    /**
     * Deletes the contents of {@code dir}. Throws an IOException if any file
     * could not be deleted, or if {@code dir} is not a readable directory.
     * Swallow the exception during running.
     */
    public static void safeDeleteDirectory(File dir) {
        try {
            deleteDirectory(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // rename methods
    public static void renameTo(File from, File to, boolean deleteDestination) throws IOException {
        if (deleteDestination) {
            deleteIfExists(to);
        }
        if (!from.renameTo(to)) {
            throw new IOException();
        }
    }

    public static void safeRenameTo(File from, File to, boolean deleteDestination) {
        try {
            renameTo(from, to, deleteDestination);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // close module


    public static void safeClose(Closeable closeable) {
        if (null == closeable) return;
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // parse byte[] by inputStream.将byte[]转换成InputStream
    public static InputStream bytesToInputStream(byte[] b) {
        ByteArrayInputStream res = new ByteArrayInputStream(b);
        return res;
    }

    /**
     * Reads all bytes from a file into a byte array.
     *
     * @param file the file to read from
     * @return a byte array containing all the bytes from file
     * @throws IllegalArgumentException if the file is bigger than the largest
     *                                  possible byte array (2^31 - 1)
     * @throws IOException              if an I/O error occurs
     */
    public static byte[] toByteArray(File file) throws IOException {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            return readFile(in, (int) in.getChannel().size());
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * Reads a file of the given expected size from the given input stream, if
     * it will fit into a byte array. This method handles the case where the file
     * size changes between when the size is read and when the contents are read
     * from the stream.
     */
    static byte[] readFile(InputStream in, int expectedSize) throws IOException {
        if (expectedSize > Integer.MAX_VALUE) {
            throw new OutOfMemoryError("file is too large to fit in a byte array: " + expectedSize + " bytes");
        }

        // some special files may return size 0 but have content, so read
        // the file normally in that case
//        return expectedSize == 0
//                ? ByteStreams.toByteArray(in)
//                : ByteStreams.toByteArray(in, (int) expectedSize);
        return inputStreamToBytes(in, expectedSize);
    }

    public static final byte[] inputStreamToBytes(InputStream inStream) {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        try {
            while ((rc = inStream.read(buff, 0, 100)) > 0) {
                swapStream.write(buff, 0, rc);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] in2b = swapStream.toByteArray();
        return in2b;
    }

    public static final byte[] inputStreamToBytes(InputStream inStream, int expectedSize) {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        int remainder = expectedSize;
        try {
            while (remainder >0 && (rc = inStream.read(buff, 0, 100)) > 0) {
                if (remainder >= rc) {
                    remainder -= rc;
                    swapStream.write(buff, 0, rc);
                } else {
                    swapStream.write(buff, 0, remainder);
                    remainder = 0;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] in2b = swapStream.toByteArray();
        return in2b;
    }

    public static String readFully(Reader reader) throws IOException {
        try {
            StringWriter writer = new StringWriter();
            char[] buffer = new char[1024];
            int count;
            while ((count = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, count);
            }
            return writer.toString();
        } finally {
            reader.close();
        }
    }


    public static void closeQuietly(/*Auto*/Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    public static void throwInterruptedIoException() throws InterruptedIOException {
        // This is typically thrown in response to an
        // InterruptedException which does not leave the thread in an
        // interrupted state, so explicitly interrupt here.
        Thread.currentThread().interrupt();
        // TODO: set InterruptedIOException.bytesTransferred
        throw new InterruptedIOException();
    }

    public static String generatorKey(String primaryKey, String secondaryKey) {
        return primaryKey + "_" + secondaryKey;
    }

    /**
     * Copies all bytes from the input stream to the output stream. Does not close or flush either
     * stream.
     *
     * @param from     the input stream to read from
     * @param to       the output stream to write to
     * @param length   the max length need to copy
     * @param buffSize buff size during copy progress.
     * @return the number of bytes copied
     * @throws IOException IOException if an I/O error occurs
     */
    public static long copy(InputStream from, OutputStream to, long length, int buffSize) throws IOException {
        checkNotNull(from);
        checkNotNull(to);
        buffSize = buffSize <= 0 ? DEFAULT_BUFF_SIZE : buffSize;
        length = length <= 0 ? Long.MAX_VALUE : length;
        byte[] buf = new byte[buffSize];
        long total = 0;
        long left = length;
        while (left > 0) {
            int r = 0;
            if (buffSize <= left) {
                r = from.read(buf);
            } else {
                r = from.read(buf, 0, (int) left);
            }
            if (r == -1) {
                break;
            }
            to.write(buf, 0, r);
            total += r;
            left -= r;
        }
        return total;
    }

    /**
     * Copies all bytes from the input stream to the output stream. Does not close or flush either
     * stream.
     *
     * @param from the input stream to read from
     * @param to   the output stream to write to
     * @return the number of bytes copied
     * @throws IOException if an I/O error occurs
     */
    public static long copy(InputStream from, OutputStream to) throws IOException {
        return copy(from, to, -1, DEFAULT_BUFF_SIZE);
    }

    /**
     * Copies all bytes from the input stream to the output stream. Does not close or flush either
     * stream.
     *
     * @param from the input stream to read from
     * @param to   the output stream to write to
     * @return the number of bytes copied
     * @throws IOException if an I/O error occurs
     */
    public static long copy(InputStream from, OutputStream to, long size) throws IOException {
        return copy(from, to, size, DEFAULT_BUFF_SIZE);
    }

    /**
     * Efficiently fetch bytes from InputStream is by delegating to
     * getBytesFromStream(is, is.available())
     */
    public static byte[] getBytesFromStream(final InputStream is) throws IOException {
        return getBytesFromStream(is, is.available());
    }

    /**
     * Efficiently fetch the bytes from the InputStream, provided that caller can guess
     * exact numbers of bytes that can be read from inputStream. Avoids one extra byte[] allocation
     * that ByteStreams.toByteArray() performs.
     *
     * @param hint - size of inputStream's content in bytes
     */
    public static byte[] getBytesFromStream(InputStream inputStream, int hint) throws IOException {
        // Subclass ByteArrayOutputStream to avoid an extra byte[] allocation and copy
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream(hint) {
            @Override
            public byte[] toByteArray() {
                // Can only use the raw buffer directly if the size is equal to the array we have.
                // Otherwise we have no choice but to copy.
                if (count == buf.length) {
                    return buf;
                } else {
                    return super.toByteArray();
                }
            }
        };
        copy(inputStream, byteOutput);
        return byteOutput.toByteArray();
    }

    /**
     * Skips exactly bytesCount bytes in inputStream unless end of stream is reached first.
     *
     * @param inputStream input stream to skip bytes from
     * @param bytesCount  number of bytes to skip
     * @return number of skipped bytes
     * @throws IOException
     */
    public static long skip(final InputStream inputStream, final long bytesCount) throws IOException {
        checkNotNull(inputStream);
        checkArgument(bytesCount >= 0);

        long toSkip = bytesCount;
        while (toSkip > 0) {
            final long skipped = inputStream.skip(toSkip);
            if (skipped > 0) {
                toSkip -= skipped;
                continue;
            }

            if (inputStream.read() != -1) {
                toSkip--;
                continue;
            }
            return bytesCount - toSkip;
        }

        return bytesCount;
    }
}
