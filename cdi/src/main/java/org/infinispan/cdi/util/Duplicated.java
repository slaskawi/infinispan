package org.infinispan.cdi.util;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface Duplicated {

    public static class Literal extends AnnotationLiteral<Duplicated> implements Duplicated {

    }

}
