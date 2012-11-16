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
import com.github.rmannibucau.cdi.cxf.api.NotSpecified;
import com.github.rmannibucau.cdi.cxf.api.Property;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

// TODO: have a look to cache
public class CxfClientProducer {
    private static final Logger LOGGER = Logger.getLogger(CxfClientProducer.class.getName());

    @Produces
    @CxfJaxWSClient
    public Object produceClient(final InjectionPoint injectionPoint) {
        final Annotated annotated = injectionPoint.getAnnotated();
        final CxfJaxWSClient annotation = annotated.getAnnotation(CxfJaxWSClient.class);

        final CxfJaxWsClientConfiguration configuration = new CxfJaxWsClientConfiguration(annotation.lazy(), annotation.wsdl(),
                parseQName(placeHolders(annotation.qname())), annotation.address(), annotation.username(), annotation.password(),
                annotation.keyStoreType(), annotation.keyManagerFactoryAlgorithm(), annotation.keyStorePassword(),
                annotation.keyStoreFile(), annotation.trustStoreAlgorithm(), annotation.trustStoreFile(),
                readProperties(annotation.properties()));

        final Class<?> type = Types.type(injectionPoint.getType());
        configuration.setServiceClass(type);

        final CxfInInterceptors inInterceptors = annotated.getAnnotation(CxfInInterceptors.class);
        if (inInterceptors != null) {
            configuration.setInInterceptors(new ArrayList<Interceptor<? extends Message>>());
            for (CxfInInterceptor interceptor : inInterceptors.value()) {
                configuration.addInInterceptor((Interceptor<? extends Message>) instantiate(interceptor.clazz(), interceptor.factoryMethod(),
                        interceptor.classname(), interceptor.properties(), interceptor.propertyFile(), interceptor.prefix()));
            }
        }

        final CxfOutInterceptors outInterceptors = annotated.getAnnotation(CxfOutInterceptors.class);
        if (outInterceptors != null) {
            configuration.setOutInterceptors(new ArrayList<Interceptor<? extends Message>>());
            for (CxfOutInterceptor interceptor : outInterceptors.value()) {
                configuration.addOutInterceptor((Interceptor<? extends Message>) instantiate(interceptor.clazz(), interceptor.factoryMethod(),
                        interceptor.classname(), interceptor.properties(), interceptor.propertyFile(), interceptor.prefix()));
            }
        }

        final CxfInFaultInterceptors infaultInterceptors = annotated.getAnnotation(CxfInFaultInterceptors.class);
        if (infaultInterceptors != null) {
            configuration.setInFaultInterceptors(new ArrayList<Interceptor<? extends Message>>());
            for (CxfInFaultInterceptor interceptor : infaultInterceptors.value()) {
                configuration.addInFaultInterceptor((Interceptor<? extends Message>) instantiate(interceptor.clazz(), interceptor.factoryMethod(),
                        interceptor.classname(), interceptor.properties(), interceptor.propertyFile(), interceptor.prefix()));
            }
        }

        final CxfOutFaultInterceptors outFaultInterceptors = annotated.getAnnotation(CxfOutFaultInterceptors.class);
        if (outFaultInterceptors != null) {
            configuration.setOutFaultInterceptors(new ArrayList<Interceptor<? extends Message>>());
            for (CxfOutFaultInterceptor interceptor : outFaultInterceptors.value()) {
                configuration.addOutFaultInterceptor((Interceptor<? extends Message>) instantiate(interceptor.clazz(), interceptor.factoryMethod(),
                        interceptor.classname(), interceptor.properties(), interceptor.propertyFile(), interceptor.prefix()));
            }
        }

        final CxfFeatures features = annotated.getAnnotation(CxfFeatures.class);
        if (features != null) {
            configuration.setFeatures(new ArrayList<Feature>());
            for (CxfFeature feature : features.value()) {
                configuration.addFeature((Feature) instantiate(feature.clazz(), feature.factoryMethod(),
                        feature.classname(), feature.properties(), feature.propertyFile(), feature.prefix()));
            }
        }

        return Proxy.newProxyInstance(ClassLoaders.current(), new Class<?>[]{ type, Client.class }, new CxfClientHandler(configuration));
    }

    private static QName parseQName(final String qname) {
        if (qname.isEmpty()) {
            return null;
        }

        final int start = qname.indexOf("{");
        final int end = qname.indexOf("}");
        if (start < 0 || end < 0) {
            return new QName(qname);
        }
        return new QName(qname.substring(start + 1, end), qname.substring(end + 1));
    }

    private static Object instantiate(final Class<?> inClazz, final String factoryMethod, final String alternativeClassName, final Property[] properties, final String propPath, final String prefix) {
        Class<?> clazz = inClazz;
        if (NotSpecified.class == clazz) {
            try {
                clazz = ClassLoaders.current().loadClass(alternativeClassName);
            } catch (ClassNotFoundException e) {
                clazz = null;
                LOGGER.log(Level.SEVERE, "Can't load " + alternativeClassName, e);
            }
        }

        if (clazz != null) {
            final Map<String, Object> attributes = new HashMap<String, Object>();
            for (Property property : properties) {
                attributes.put(property.key(), property.value());
            }

            final Map<String, Object> fileProps = readProperties(propPath);
            if (fileProps != null) {
                final int len = prefix.length();
                for (Map.Entry<String, Object> entry : fileProps.entrySet()) {
                    String key = entry.getKey();
                    if (key.startsWith(prefix)) {
                        key = key.substring(len);
                        attributes.put(key, entry.getValue());
                    }
                }
            }

            final ObjectRecipe recipe;
            if (!factoryMethod.isEmpty()) {
                recipe = new ObjectRecipe(clazz, factoryMethod);
            } else {
                recipe = new ObjectRecipe(clazz);
            }

            recipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
            recipe.allow(Option.IGNORE_MISSING_PROPERTIES);
            recipe.setAllProperties(attributes);

            return recipe.create();
        }

        throw new IllegalArgumentException("Can't instantiate " + alternativeClassName);
    }

    private static Map<String, Object> readProperties(final String propPath) {
        if (propPath == null || propPath.isEmpty()) {
            return null;
        }

        final String realPath = placeHolders(propPath);

        final ClassLoader cl = ClassLoaders.current();
        InputStream is = cl.getResourceAsStream(realPath);
        if (is == null) {
            final File file = new File(realPath);
            if (file.exists()) {
                try {
                    is = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    LOGGER.log(Level.SEVERE, "Can't read file " + realPath);
                }
            }
        }

        if (is != null) {
            final Properties prop = new Properties();
            try {
                prop.load(is);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can't load file " + realPath);
            }
            if (prop.size() > 0) {
                final Map<String, Object> properties = new HashMap<String, Object>();
                for (Map.Entry<Object, Object> entry : prop.entrySet()) {
                    properties.put(entry.getKey().toString(), entry.getValue());
                }
                return properties;
            }
        } else {
            LOGGER.log(Level.SEVERE, "Can't find " + propPath);
        }

        return null;
    }

    private static String placeHolders(final String value) {
        if (value == null) {
            return null;
        }

        String path = value;
        int start = path.indexOf("${");
        int end = path.indexOf("}", Math.max(start, 0));
        if (start >= 0 && end > 0) {
            do {
                final String key = path.substring(start + 2, end);
                final String prop = System.getProperty(key);
                if (prop == null) {
                    LOGGER.warning("can't find property " + key);
                    break;
                }

                path = path.replace(path.substring(start, end + 1), prop);

                start = path.indexOf("${");
                end = path.indexOf("}", Math.max(start, 0));
            } while (start > 0 && end > 0);
        }
        return path;
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
            factory.setWsdlURL(placeHolders(configuration.getWsdl()));
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
            if (configuration.getOutInterceptors() != null) {
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

            if (configuration.hasSSL()) {
                final Client client = ClientProxy.getClient(delegate);
                final HTTPConduit conduit = (HTTPConduit) client.getConduit();
                conduit.setTlsClientParameters(buildTlsParameters());
            }
        }

        private TLSClientParameters buildTlsParameters() {
            final TLSClientParameters tlsParameters = new TLSClientParameters();

            final char[] pwd = placeHolders(configuration.getKeyStorePassword()).toCharArray();

            final KeyStore keyStore;
            try {
                if (configuration.getKeyStoreType() != null) {
                    keyStore = KeyStore.getInstance(configuration.getKeyStoreType());
                } else {
                    keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                }
            } catch (KeyStoreException e) {
                throw new RuntimeException(e);
            }

            try {
                keyStore.load(new FileInputStream(placeHolders(configuration.getKeyStoreFile())), pwd);

                final KeyManagerFactory keyManagerFactory;
                if (configuration.getKeyManagerFactoryAlgorithm() != null) {
                    keyManagerFactory = KeyManagerFactory.getInstance(configuration.getKeyManagerFactoryAlgorithm());
                } else {
                    keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                }

                keyManagerFactory.init(keyStore, pwd);

                tlsParameters.setKeyManagers(keyManagerFactory.getKeyManagers());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            try {
                keyStore.load(new FileInputStream(placeHolders(configuration.getTrustStoreFile())), pwd);

                final TrustManagerFactory trustManagerFactory;
                if (configuration.getKeyManagerFactoryAlgorithm() != null) {
                    trustManagerFactory = TrustManagerFactory.getInstance(configuration.getTrustStoreAlgorithm());
                } else {
                    trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                }

                trustManagerFactory.init(keyStore);

                tlsParameters.setTrustManagers(trustManagerFactory.getTrustManagers());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return tlsParameters;
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
