package org.infinispan.client.hotrod.impl.transport.tcp;

import java.io.IOException;
import java.io.InputStream;

import javax.security.sasl.SaslClient;

import org.infinispan.client.hotrod.logging.Log;
import org.infinispan.client.hotrod.logging.LogFactory;
import org.infinispan.commons.util.Util;

/**
 * SaslInputStream.
 *
 * @author Tristan Tarrant
 * @since 7.0
 */
public class SaslInputStream extends InputStream {
   public static final Log LOG = LogFactory.getLog(SaslInputStream.class);

   private final SaslClient saslClient;
   private final InputStream is;
   private final byte buf[] = new byte[1];

   public SaslInputStream(InputStream is, SaslClient saslClient) {
      this.is = is;
      this.saslClient = saslClient;
   }

   @Override
   public int read() throws IOException {
      int b = is.read();
      buf[0] = (byte)b;
      byte[] wrap = saslClient.wrap(buf, 0, 1);
      return wrap[0];
   }

   @Override
   public int read(byte[] b) throws IOException {
      return read(b, 0, b.length);
   }

   @Override
   public int read(byte[] b, int off, int len) throws IOException {
      byte buf[] = new byte[len];
      int isLen = is.read(buf);
      byte[] unwrap = saslClient.unwrap(buf, 0, isLen);
      System.arraycopy(unwrap, 0, b, off, isLen);
      return isLen;
   }

   @Override
   public long skip(long n) throws IOException {
      return is.skip(n);
   }

   @Override
   public int available() throws IOException {
      return is.available();
   }

   @Override
   public void close() throws IOException {
      Util.close(is);
      saslClient.dispose();
   }

   @Override
   public boolean markSupported() {
      return false;
   }

}
