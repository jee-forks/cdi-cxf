package com.github.rmannibucau.cdi.cxf.impl;

import com.github.rmannibucau.cdi.cxf.api.CxfJaxWSClient;
import org.apache.deltaspike.core.util.bean.BeanBuilder;
import org.apache.deltaspike.core.util.bean.ImmutableBeanWrapper;
import org.apache.deltaspike.core.util.metadata.AnnotationInstanceProvider;
import org.apache.deltaspike.core.util.metadata.builder.DummyInjectionTarget;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessProducerMethod;
import java.util.Collection;
import java.util.HashSet;

public class CxfClientExtension implements Extension {
    private final Collection<Class<?>> clients = new HashSet<Class<?>>();
    private Bean<Object> bundleProducerBean;

    protected <X> void findCxfJaxWsClients(final @Observes ProcessAnnotatedType<X> processAnnotatedType) {
        final AnnotatedType<X> type = processAnnotatedType.getAnnotatedType();
        for (AnnotatedField<?> field : type.getFields()) {
            if (field.isAnnotationPresent(CxfJaxWSClient.class)) {
                clients.add(Types.type(field.getBaseType()));
            }
        }
    }

    protected void addClientBeans(final @Observes AfterBeanDiscovery abd, final BeanManager beanManager) {
        for (Class<?> type : clients) {
            abd.addBean(new BeanBuilder<Object>(beanManager)
                    .beanClass(type)
                    .scope(Dependent.class)
                    .alternative(true)
                    .injectionPoints(new DummyInjectionTarget<Object>().getInjectionPoints())
                    .types(type, Object.class)
                    .qualifiers(AnnotationInstanceProvider.of(CxfJaxWSClient.class), AnnotationInstanceProvider.of(Any.class))
                    .beanLifecycle(new CxfJaxWSClientLifecycle<Object>(bundleProducerBean))
                    .id(ImmutableBeanWrapper.class.getName() + ":" + type.getName() + ":" + CxfJaxWSClient.class.getName())
                    .create());
        }
    }

    protected void detectProducers(final @Observes ProcessProducerMethod<Object, CxfClientProducer> event) {
        if (event.getAnnotatedProducerMethod().isAnnotationPresent(CxfJaxWSClient.class)) {
            final Bean<?> bean = event.getBean();
            bundleProducerBean = (Bean<Object>) bean;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected void cleanup(final @Observes AfterDeploymentValidation afterDeploymentValidation) {
        clients.clear();
    }
}
