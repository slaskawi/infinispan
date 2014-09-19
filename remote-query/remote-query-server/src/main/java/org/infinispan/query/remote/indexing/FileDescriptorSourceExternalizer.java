package org.infinispan.query.remote.indexing;

import org.infinispan.commons.io.UnsignedNumeric;
import org.infinispan.commons.marshall.AbstractExternalizer;
import org.infinispan.commons.util.Util;
import org.infinispan.protostream.FileDescriptorSource;

import java.io.*;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Marshaller for FileDescriptorSource.
 *
 * @author gustavonalle
 * @since 7.0
 */
public class FileDescriptorSourceExternalizer extends AbstractExternalizer<FileDescriptorSource> {

   @Override
   @SuppressWarnings("unchecked")
   public Set<Class<? extends FileDescriptorSource>> getTypeClasses() {
      return Util.<Class<? extends FileDescriptorSource>>asSet(FileDescriptorSource.class);
   }

   @Override
   public void writeObject(ObjectOutput output, FileDescriptorSource object) throws IOException {
      Map<String, char[]> fileDescriptors = object.getFileDescriptors();
      Set<Map.Entry<String, char[]>> entries = fileDescriptors.entrySet();
      UnsignedNumeric.writeUnsignedInt(output, entries.size());
      for (Map.Entry<String, char[]> entry : fileDescriptors.entrySet()) {
         String key = entry.getKey();
         char[] value = entry.getValue();
         output.writeUTF(key);
         UnsignedNumeric.writeUnsignedInt(output, value.length);
         byte[] compressed = compress(value);
         UnsignedNumeric.writeUnsignedInt(output, compressed.length);
         output.write(compressed);
      }
   }

   @Override
   public FileDescriptorSource readObject(ObjectInput input) throws IOException, ClassNotFoundException {
      FileDescriptorSource fileDescriptorSource = new FileDescriptorSource();
      int size = UnsignedNumeric.readUnsignedInt(input);
      for (int i = 0; i < size; i++) {
         String name = input.readUTF();
         int length = UnsignedNumeric.readUnsignedInt(input);
         int compressedLength = UnsignedNumeric.readUnsignedInt(input);
         byte[] compressed = new byte[compressedLength];
         input.readFully(compressed);
         char[] contents = decompress(compressed, length);
         fileDescriptorSource.addProtoFile(name, String.valueOf(contents));
      }
      return fileDescriptorSource;
   }

   public byte[] compress(char[] input) throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      GZIPOutputStream zip = new GZIPOutputStream(baos);
      Writer writer = new OutputStreamWriter(zip, "UTF-8");
      try {
         writer.write(input);
         writer.flush();
         zip.finish();
         return baos.toByteArray();
      }  finally {
         writer.close();
      }
   }

   private char[] decompress(byte[] input, int size) throws IOException {
      char[] cbuf = new char[size];
      Reader reader = new InputStreamReader(new GZIPInputStream(new ByteArrayInputStream(input)), "UTF-8");
      try {
         int off = 0;
         while (size > 0) {
            int n = reader.read(cbuf, off, size);
            if (n < 0) {
               throw new EOFException();
            }
            off += n;
            size -= n;
         }
         return cbuf;
      } finally {
         reader.close();
      }
   }
}
