package org.dolphin.lib.binaryresource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * A trivial implementation of BinaryResource that wraps a byte array
 */
public class ByteArrayBinaryResource implements BinaryResource {
  private final byte[] mBytes;

  public ByteArrayBinaryResource(byte[] bytes) {
    mBytes = Preconditions.checkNotNull(bytes);
  }

  @Override
  public long size() {
    return mBytes.length;
  }

  @Override
  public InputStream openStream() throws IOException {
    return new ByteArrayInputStream(mBytes);
  }

  /**
   * Get the underlying byte array
   * @return the underlying byte array of this resource
   */
  @Override
  public byte[] read() {
    return mBytes;
  }
}
