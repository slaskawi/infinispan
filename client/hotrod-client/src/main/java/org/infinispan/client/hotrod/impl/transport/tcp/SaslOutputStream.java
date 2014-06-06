package org.infinispan.client.hotrod.impl.transport.tcp;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.security.sasl.SaslClient;

import org.infinispan.commons.util.Util;

/**
 * SaslOutputStream.
 *
 * @author Tristan Tarrant
 * @since 7.0
 */
public class SaslOutputStream extends OutputStream {

   private static final int BUFFER_SIZE = 64 * 1024;

   private final OutputStream os;
   private final SaslClient saslClient;
   private final byte buf[] = new byte[1];

   public SaslOutputStream(OutputStream outStream, SaslClient saslClient) {
      this.saslClient = saslClient;
      this.os = new BufferedOutputStream(outStream, BUFFER_SIZE);
   }

   @Override
   public void write(int b) throws IOException {
      buf[0] = (byte) b;
      write(buf, 0, 1);
   }

   @Override
   public void write(byte[] b) throws IOException {
      write(b, 0, b.length);
   }

   @Override
   public void write(byte[] inBuf, int off, int len) throws IOException {
      os.write(saslClient.wrap(inBuf, off, len));
   }

   @Override
   public void flush() throws IOException {
      os.flush();
   }

   @Override
   public void close() throws IOException {
      Util.close(os);
      saslClient.dispose();
   }
}