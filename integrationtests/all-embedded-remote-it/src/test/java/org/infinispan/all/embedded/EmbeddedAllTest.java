package org.infinispan.all.embedded;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Test;

/**
 * Self standing functional tests for infinispan-embedded UberJar.
 *
 * @author Tomas Sykora (tsykora@redhat.com)
 */
public class EmbeddedAllTest {

   @Test
   public void should() throws Exception {
      WeldContainer weld = new Weld().initialize();


   }


}
