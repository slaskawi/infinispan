package org.infinispan.persistence.jdbc;

import java.beans.PropertyEditorSupport;

/**
 * Note: Do not rename this class otherwise it won't by found by JDK.
 */
public class DatabaseTypeEditor extends PropertyEditorSupport {

   private DatabaseType databaseType;

   @Override
   public Object getValue() {
      return databaseType;
   }

   @Override
   public void setAsText(String text) throws IllegalArgumentException {
      databaseType = DatabaseType.valueOf(text);
   }
}
