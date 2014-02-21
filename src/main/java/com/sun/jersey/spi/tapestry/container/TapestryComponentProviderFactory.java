package com.sun.jersey.spi.tapestry.container;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.ws.rs.WebApplicationException;

import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.annotations.Scope;

import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ioc.*;
import com.sun.jersey.spi.inject.Inject;

public class TapestryComponentProviderFactory implements IoCComponentProviderFactory {

    private static final Logger LOGGER = Logger.getLogger(TapestryComponentProviderFactory.class.getName());

    private final Registry registry;

    public TapestryComponentProviderFactory(ResourceConfig rc, Registry registry) {
        this.registry = registry;
    }

    private final Map<String, ComponentScope> scopeMap = createScopeMap();

    private Map<String, ComponentScope> createScopeMap() {
        Map<String, ComponentScope> m = new HashMap<String, ComponentScope>();
        m.put(ScopeConstants.DEFAULT, ComponentScope.Singleton);
        m.put(ScopeConstants.PERTHREAD, ComponentScope.PerRequest);
        return m;
    }

    private ComponentScope getComponentScope(String scope) {
        ComponentScope cs = scopeMap.get(scope);
        return (cs != null) ? cs : ComponentScope.Undefined;
    }

    /**
     * Determine if a class uses field or method injection via Tapestry IOC
     * using the {@code Inject} annotation
     *
     * @param c the class.
     * @return true if the class is an implicit Tapestry service.
     */
    public boolean isTapestryFieldOrMethodInjected(Class<?> c) {
        for (Method m : c.getDeclaredMethods()) {
            if (isInjectable(m)) {
                return true;
            }
        }

        for (Field f : c.getDeclaredFields()) {
            if (isInjectable(f)) {
                return true;
            }
        }

        return !c.equals(Object.class) && isTapestryFieldOrMethodInjected(c.getSuperclass());
    }

    public static boolean isInjectable(AnnotatedElement element) {
        return (element.isAnnotationPresent(org.apache.tapestry5.ioc.annotations.Inject.class)
                || element.isAnnotationPresent(org.apache.tapestry5.ioc.annotations.InjectService.class)
                || element.isAnnotationPresent(javax.inject.Inject.class));
    }

    private static <T extends Annotation> T getAnnotation(Annotation[] annotations,
                                                          Class<T> clazz) {
        if (annotations != null) {
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(clazz)) {
                    return clazz.cast(annotation);
                }
            }
        }
        return null;
    }

    private static String getBeanName(ComponentContext cc, Class<?> c, Registry registry) {
        boolean annotatedWithInject = false;
        if (cc != null) {
            final Inject inject = getAnnotation(cc.getAnnotations(), Inject.class);
            if (inject != null) {
                annotatedWithInject = true;
                if (inject.value() != null && !inject.value().equals("")) {
                    return inject.value();
                }

            }

            final InjectParam injectParam = getAnnotation(cc.getAnnotations(), InjectParam.class);
            if (injectParam != null) {
                annotatedWithInject = true;
                if (injectParam.value() != null && !injectParam.value().equals("")) {
                    return injectParam.value();
                }

            }
        }
        return null;
    }

    @Override
    public IoCComponentProvider getComponentProvider(Class<?> aClass) {
        return getComponentProvider(null, aClass);
    }

    @Override
    public IoCComponentProvider getComponentProvider(ComponentContext componentContext, Class<?> aClass) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "getComponentProvider({0})", aClass.getName());
        }

        String _scope = ScopeConstants.DEFAULT;
        final Scope scope = (Scope) aClass.getAnnotation(Scope.class);
        if (scope != null) {
            _scope = scope.value();
        }

        if (scope != null) {
            _scope = scope.value();
        }

        String beanName = getBeanName(componentContext, aClass, registry);

        Named annotation = aClass.getAnnotation(Named.class);

        if (annotation != null) {
            beanName = annotation.value();
        }

        if (isTapestryFieldOrMethodInjected(aClass)) {
            LOGGER.log(Level.INFO, "Binding {0} to TapestryManagedComponentProvider", aClass.getName());
            //return new TapestryManagedComponentProvider(getComponentScope(_scope), beanName, aClass, registry);
            return new TapestryInjectedComponentProvider(registry);
        } else {
            return null;
        }
    }

    private class TapestryManagedComponentProvider implements IoCManagedComponentProvider {

        private final ComponentScope scope;
        private final String beanName;
        private final Class c;
        private final Registry registry;

        TapestryManagedComponentProvider(ComponentScope scope, String beanName, Class c, Registry registry) {
            this.scope = scope;
            this.beanName = beanName;
            this.c = c;
            this.registry = registry;
        }

        @Override
        public ComponentScope getScope() {
            return scope;
        }

        @Override
        public Object getInjectableInstance(Object o) {
            return o;
        }

        @Override
        public Object getInstance() {
            if (beanName != null) {
                return registry.getService(beanName, c);
            } else {
                return registry.getService(c);
            }
        }
    }

    final class JerseyAnnotationProvider implements AnnotationProvider
    {

        private final Field f;

        JerseyAnnotationProvider(Field f) {
            this.f = f;
        }

        public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
        {
            return f.getAnnotation(annotationClass);
        }
    };

    private static class TapestryInjectedComponentProvider
            implements IoCProxiedComponentProvider {

        private final Registry registry;

        public TapestryInjectedComponentProvider(Registry registry) {
            this.registry = registry;
        }

        @Override
        public Object getInstance() {
            throw new IllegalStateException();
        }

        private synchronized static void inject(Object target, Field field, Object value)
        {
            try
            {
                if (!field.isAccessible())
                    field.setAccessible(true);

                field.set(target, value);

                // Is there a need to setAccessible back to false?
            } catch (Exception ex)
            {
                throw new RuntimeException(String.format("Unable to set field '%s' of %s to %s: %s", field.getName(),
                        target, value, ex.getLocalizedMessage()));
            }
        }

        private void injectFields(Object o) throws IllegalAccessException {
            injectFields(o, o.getClass());
        }

        private void injectFields(Object o, Class<?> clazz) throws IllegalAccessException {
            for (final Field f : clazz.getDeclaredFields()) {
                if (isInjectable(f)) {

                    final AnnotationProvider ap = new AnnotationProvider()
                    {
                        public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
                        {
                            return f.getAnnotation(annotationClass);
                        }
                    };
                    inject(o, f, registry.getObject(f.getType(), ap));
                }
            }

            if (clazz.getSuperclass() != null) {
                injectFields(o, clazz.getSuperclass());
            }
        }

        @Override
        public Object proxy(Object o) {
            try {
                injectFields(o);
            } catch (IllegalAccessException e) {
                if (e.getCause() instanceof WebApplicationException) {
                    throw (WebApplicationException)e.getCause();
                }
                throw new WebApplicationException(e);
            }
            return o;
        }
    }

}
