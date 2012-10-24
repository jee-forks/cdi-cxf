package com.github.rmannibucau.cdi.cxf.impl;

import com.github.rmannibucau.cdi.cxf.api.CxfFeature;
import com.github.rmannibucau.cdi.cxf.api.CxfFeatures;
import com.github.rmannibucau.cdi.cxf.api.CxfInFaultInterceptor;
import com.github.rmannibucau.cdi.cxf.api.CxfInFaultInterceptors;
import com.github.rmannibucau.cdi.cxf.api.CxfInInterceptor;
import com.github.rmannibucau.cdi.cxf.api.CxfInInterceptors;
import com.github.rmannibucau.cdi.cxf.api.CxfJaxWSClient;
import com.github.rmannibucau.cdi.cxf.api.CxfOutFaultInterceptor;
import com.github.rmannibucau.cdi.cxf.api.CxfOutFaultInterceptors;
import com.github.rmannibucau.cdi.cxf.api.CxfOutInterceptor;
import com.github.rmannibucau.cdi.cxf.api.CxfOutInterceptors;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.InjectionPoint;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.logging.Level;
import java.util.logging.Logger;

// TODO: have a look to cache
public class CxfClientProducer {
    private static final Logger LOGGER = Logger.getLogger(CxfClientProducer.class.getName());

    @Produces
    @CxfJaxWSClient
    public Object produceClient(final InjectionPoint injectionPoint) {
        final Annotated annotated = injectionPoint.getAnnotated();
        final CxfJaxWsClientConfiguration configuration = new CxfJaxWsClientConfiguration(annotated.getAnnotation(CxfJaxWSClient.class));
        final Class<?> type = Types.type(injectionPoint.getType());
        configuration.setServiceClass(type);

        final CxfInInterceptors inInterceptors = annotated.getAnnotation(CxfInInterceptors.class);
        if (inInterceptors != null) {
            for (CxfInInterceptor interceptor : inInterceptors.value()) {
                configuration.addInInterceptor(interceptor);
            }
        }

        final CxfOutInterceptors outInterceptors = annotated.getAnnotation(CxfOutInterceptors.class);
        if (inInterceptors != null) {
            for (CxfOutInterceptor interceptor : outInterceptors.value()) {
                configuration.addOutInterceptor(interceptor);
            }
        }

        final CxfInFaultInterceptors infaultInterceptors = annotated.getAnnotation(CxfInFaultInterceptors.class);
        if (infaultInterceptors != null) {
            for (CxfInFaultInterceptor interceptor : infaultInterceptors.value()) {
                configuration.addInFaultInterceptor(interceptor);
            }
        }

        final CxfOutFaultInterceptors outFaultInterceptors = annotated.getAnnotation(CxfOutFaultInterceptors.class);
        if (inInterceptors != null) {
            for (CxfOutFaultInterceptor interceptor : outFaultInterceptors.value()) {
                configuration.addOutFaultInterceptor(interceptor);
            }
        }

        final CxfFeatures features = annotated.getAnnotation(CxfFeatures.class);
        if (inInterceptors != null) {
            for (CxfFeature feature : features.value()) {
                configuration.addFeature(feature);
            }
        }

        return Proxy.newProxyInstance(ClassLoaders.current(), new Class<?>[]{ type, Client.class }, new CxfClientHandler(configuration));
    }

    public static class CxfClientHandler implements InvocationHandler {
        private final CxfJaxWsClientConfiguration configuration;
        private Object delegate = null;

        public CxfClientHandler(final CxfJaxWsClientConfiguration configuration) {
            this.configuration = configuration;
            if (!configuration.isLazy()) {
                buildDelegate();
            }
        }

        private void buildDelegate() {
            final JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
            factory.setServiceClass(configuration.getServiceClass());
            factory.setWsdlURL(configuration.getWsdl());
            factory.setServiceName(configuration.getQname());
            factory.setAddress(configuration.getAddress());
            factory.setUsername(configuration.getUsername());
            factory.setPassword(configuration.getPassword());
            factory.setProperties(configuration.getProperties());

            if (configuration.getInInterceptors() != null) {
                factory.setInInterceptors(configuration.getInInterceptors());
            }
            if (configuration.getInFaultInterceptors() != null) {
                factory.setInFaultInterceptors(configuration.getInFaultInterceptors());
            }
            if (configuration.getOutFaultInterceptors() != null) {
                factory.setOutInterceptors(configuration.getOutInterceptors());
            }
            if (configuration.getOutFaultInterceptors() != null) {
                factory.setOutFaultInterceptors(configuration.getOutFaultInterceptors());
            }
            if (configuration.getFeatures() != null) {
                factory.setFeatures(configuration.getFeatures());
            }

            try {
                delegate = factory.create(configuration.getServiceClass());
            } catch (RuntimeException re) {
                LOGGER.log(Level.SEVERE, re.getMessage(), re);
                throw re;
            }
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            if (delegate == null) {
                synchronized (this) {
                    if (delegate == null) {
                        buildDelegate();
                    }
                }
            }

            return method.invoke(delegate, args);
        }

        public Client getClient() {
            if (delegate != null) {
                return ClientProxy.getClient(delegate);
            }
            return null;
        }
    }
}
