package org.infinispan.cdi.util;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * <p>
 * A base class for implementing a {@link PassivationCapable} {@link Bean}. The
 * attributes are immutable, and collections are defensively copied on
 * instantiation. It uses the defaults from the specification for properties if
 * not specified.
 * </p>
 * <p/>
 * <p>
 * This bean delegates it's lifecycle to the callbacks on the provided
 * {@link ContextualLifecycle}.
 * </p>
 *
 * @author Stuart Douglas
 * @author Pete Muir
 * @see ImmutableBean
 * @see BeanBuilder
 */
public class ImmutablePassivationCapableBean<T> extends ImmutableBean<T> implements PassivationCapable {
    private final String id;

    public ImmutablePassivationCapableBean(String id, Class<?> beanClass, String name, Set<Annotation> qualifiers, Class<? extends Annotation> scope, Set<Class<? extends Annotation>> stereotypes, Set<Type> types, boolean alternative, boolean nullable, Set<InjectionPoint> injectionPoints, ContextualLifecycle<T> beanLifecycle, String toString) {
        super(beanClass, name, qualifiers, scope, stereotypes, types, alternative, nullable, injectionPoints, beanLifecycle, toString);
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ImmutablePassivationCapableBean that = (ImmutablePassivationCapableBean) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}
