package com.github.rmannibucau.cdi.cxf.impl;

import org.apache.cxf.endpoint.Client;
import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import java.lang.reflect.Proxy;

public class CxfJaxWSClientLifecycle<X> implements ContextualLifecycle<X> {
    private final Bean<X> delegate;

    public CxfJaxWSClientLifecycle(final Bean<X> bundleProducerBean) {
        delegate = bundleProducerBean;
    }

    @Override
    public X create(final Bean<X> bean, final CreationalContext<X> creationalContext) {
        return delegate.create(creationalContext);
    }

    @Override
    public void destroy(final Bean<X> bean, X instance, final CreationalContext<X> creationalContext) {
        final Client client = ((CxfClientProducer.CxfClientHandler) Proxy.getInvocationHandler(instance)).getClient();
        if (client != null) {
            client.destroy();
        }
        delegate.destroy(instance, creationalContext);
    }
}
