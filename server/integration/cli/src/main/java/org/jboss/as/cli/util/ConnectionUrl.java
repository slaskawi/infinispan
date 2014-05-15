package org.jboss.as.cli.util;

import org.jboss.as.cli.CommandFormatException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * All the properties existing in a connection URL.
 *
 * @author Pedro Ruivo
 * @since 6.1
 */
public class ConnectionUrl {

   public static final ConnectionUrl DEFAULT = new ConnectionUrl(null, -1, null, null, null, null);
   //copy from JMXRemotingUrl class.
   private static final Pattern JMX_URL = Pattern.compile("^(?:(?![^:@]+:[^:@/]*@)(remoting|jmx):)?(?://)?((?:(([^:@]*):?([^:@]*))?@)?(\\[[0-9A-Fa-f:]+\\]|[^:/?#]*)(?::(\\d*))?)(?:/([^/]*)(?:/(.*))?)?");
   private final String host;
   private final int port;
   private final String user;
   private final char[] pass;
   private final String container;
   private final String cache;

   public ConnectionUrl(String host, int port, String user, char[] pass, String container, String cache) {
      this.host = host;
      this.port = port;
      this.user = user;
      this.pass = pass;
      this.container = container;
      this.cache = cache;
   }

   public String getHost() {
      return host;
   }

   public int getPort() {
      return port;
   }

   public String getUser() {
      return user;
   }

   public char[] getPass() {
      return pass;
   }

   public String getContainer() {
      return container;
   }

   public String getCache() {
      return cache;
   }

   public static ConnectionUrl parse(String connection) throws CommandFormatException {
      String host;
      int port = -1;
      String username;
      char[] password = null;
      String container;
      String cache;
      Matcher matcher = JMX_URL.matcher(connection);
      if (!matcher.matches()) {
         throw new IllegalArgumentException(connection);
      }
      username = nullIfEmpty(matcher.group(4));
      String pass = nullIfEmpty(matcher.group(5));
      if (pass != null) {
         password = pass.toCharArray();
      }
      host = nullIfEmpty(matcher.group(6));
      String portS = nullIfEmpty(matcher.group(7));
      if (portS != null) {
         try {
            port = Integer.parseInt(portS);
         } catch (NumberFormatException e) {
            throw new CommandFormatException("The port must be a valid non-negative integer: '" + connection + "'");
         }
         if (port < 0) {
            throw new CommandFormatException("The port must be a valid non-negative integer: '" + connection + "'");
         }
      }
      container = nullIfEmpty(matcher.group(8));
      cache = nullIfEmpty(matcher.group(9));
      return new ConnectionUrl(host, port, username, password, container, cache);
   }

   private static String nullIfEmpty(String s) {
      if (s != null && s.length() == 0) {
         return null;
      } else {
         return s;
      }
   }

}
