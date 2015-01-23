package org.infinispan.jcache.annotation;

import javax.cache.annotation.CacheRemoveAll;
import javax.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@InterceptorBinding
@CacheRemoveAll
public @interface CacheRemoveAllInterceptorBinding {
}
