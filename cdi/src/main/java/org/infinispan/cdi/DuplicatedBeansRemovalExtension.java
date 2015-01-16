package org.infinispan.cdi;

import org.infinispan.cdi.util.Duplicated;
import org.infinispan.cdi.util.annotatedtypebuilder.AnnotatedTypeBuilder;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The Infinispan CDI extension class.
 *
 * @author Pete Muir
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public class DuplicatedBeansRemovalExtension implements Extension {

   private ConcurrentMap<Bean<?>, Boolean> duplicates = new ConcurrentHashMap<Bean<?>, Boolean>();

   private Set<String> d = new HashSet<String>();

//   private static final Log log = LogFactory.getLog(DuplicatedBeansRemovalExtension.class, Log.class);

   void decorateServlet(@Observes ProcessInjectionTarget<?> pit) {
      System.out.println("PIT: " + pit.getInjectionTarget().getInjectionPoints());

   }

   <X> void processAnnotatedType(@Observes ProcessAnnotatedType<X> event, BeanManager beanManager) {



      Set<Annotation> annotations = event.getAnnotatedType().getAnnotations();
      Set<Annotation> qualifiers = new HashSet<Annotation>();
      for(Annotation a : annotations) {
         if(beanManager.isQualifier(a.getClass())) {
            qualifiers.add(a);
         }
      }

      Set<Bean<?>> beans = beanManager.getBeans(event.getAnnotatedType().getBaseType(), qualifiers.toArray(new Annotation[]{}));

      String className = event.getAnnotatedType().getJavaClass().getName().replaceAll("\\$\\d+", "");

      System.out.println("BEANS: " + beans);

      System.out.println("class" + className + " " + d.contains(className) + " " + !beans.isEmpty());
      if(d.contains(className) || !beans.isEmpty()) {
         System.out.println("JEST" + className);
         AnnotatedTypeBuilder<X> xAnnotatedTypeBuilder = new AnnotatedTypeBuilder<X>().readFromType(event.getAnnotatedType());
         xAnnotatedTypeBuilder.addToClass(new Duplicated.Literal());
         event.veto();
      } else {
         d.add(className);
      }

   }

}
