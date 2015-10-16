package org.dolphin.lib.references;

import org.dolphin.lib.Log;
import org.dolphin.lib.Preconditions;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Created by hanyanan on 2015/10/16.
 */
public class CloseableReference<T> implements Closeable {
    private static final String TAG = "CloseableReference";
    private static final Log log = new Log();
    /**
     * default closable releaser.
     */
    private static final ResourceReleaser<Closeable> DEFAULT_CLOSEABLE_RELEASER =
            new ResourceReleaser<Closeable>() {
                @Override
                public void release(Closeable value) {
                    try {
                        Closeables.close(value, true);
                    } catch (IOException ioe) {
                        // This will not happen, Closeable.close swallows and logs IOExceptions
                    }
                }
            };

    private static final ResourceReleaser<HttpURLConnection> DEFAULT_CONNECTION_RELEASER =
            new ResourceReleaser<HttpURLConnection>() {
                @Override
                public void release(HttpURLConnection value) {
                    value.disconnect();
                }
            };

    private boolean isClosed = false;
    private T value;
    ResourceReleaser<T> resourceReleaser;

    private CloseableReference(T closeable, ResourceReleaser<T> resourceReleaser) {
        this.value = closeable;
        this.resourceReleaser = resourceReleaser;
    }

    /**
     * Constructs a CloseableReference.
     * <p/>
     * <p>Returns null if the parameter is null.
     */
    public static <T extends Closeable> CloseableReference<T> of(T closable) {
        if (closable == null) {
            return null;
        } else {
            return new CloseableReference(closable, DEFAULT_CLOSEABLE_RELEASER);
        }
    }

    /**
     * Constructs a CloseableReference.
     * <p/>
     * <p>Returns null if the parameter is null.
     */
    public static <T extends HttpURLConnection> CloseableReference<T> of(T connection) {
        if (connection == null) {
            return null;
        } else {
            return new CloseableReference(connection, DEFAULT_CONNECTION_RELEASER);
        }
    }

    /**
     * Constructs a CloseableReference (wrapping a SharedReference) of T with provided
     * ResourceReleaser<T>. If t is null, this will just return null.
     */
    public static
    @Nullable
    <T> CloseableReference<T> of(@Nullable T t, ResourceReleaser<T> resourceReleaser) {
        if (t == null) {
            return null;
        } else {
            return new CloseableReference<T>(t, resourceReleaser);
        }
    }

    /**
     * Closes this CloseableReference.
     * <p/>
     * <p>Decrements the reference count of the underlying object. If it is zero, the object
     * will be released.
     * <p/>
     * <p>This method is idempotent. Calling it multiple times on the same instance has no effect.
     */
    @Override
    public void close() {
        synchronized (this) {
            if (isClosed) {
                return;
            }
            isClosed = true;
        }

        resourceReleaser.release(value);
        value = null;
    }

    /**
     * Checks if this closable-reference is valid i.e. is not closed.
     *
     * @return true if the closeable reference is valid
     */
    public synchronized boolean isValid() {
        return !isClosed;
    }


    /**
     * Returns the underlying Closeable if this reference is not closed yet.
     * Otherwise IllegalStateException is thrown.
     */
    public synchronized T get() {
        Preconditions.checkState(!isClosed);
        return value;
    }

    /**
     * Method used for tracking Closeables pointed by CloseableReference.
     * Use only for debugging and logging.
     */
    public synchronized int getValueHash() {
        return isValid() ? System.identityHashCode(value) : 0;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            // We put synchronized here so that lint doesn't warn about accessing mIsClosed, which is
            // guarded by this. Lint isn't aware of finalize semantics.
            synchronized (this) {
                if (isClosed) {
                    return;
                }
            }

            log.w(TAG, "Finalized without closing: %x (type = %s)",
                    String.valueOf(System.identityHashCode(this)),
                    value.getClass().getSimpleName());

            close();
        } finally {
            super.finalize();
        }
    }

    /**
     * Closes the reference handling null.
     *
     * @param ref the reference to close
     */
    public static void closeSafely(@Nullable CloseableReference<?> ref) {
        if (ref != null) {
            ref.close();
        }
    }

    /**
     * Closes the references in the iterable handling null.
     *
     * @param references the reference to close
     */
    public static void closeSafely(@Nullable Iterable<? extends CloseableReference<?>> references) {
        if (references != null) {
            for (CloseableReference<?> ref : references) {
                closeSafely(ref);
            }
        }
    }
}
