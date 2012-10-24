package com.github.rmannibucau.cdi.cxf.impl;

import com.github.rmannibucau.cdi.cxf.api.CxfFeature;
import com.github.rmannibucau.cdi.cxf.api.CxfInFaultInterceptor;
import com.github.rmannibucau.cdi.cxf.api.CxfInInterceptor;
import com.github.rmannibucau.cdi.cxf.api.CxfJaxWSClient;
import com.github.rmannibucau.cdi.cxf.api.CxfOutFaultInterceptor;
import com.github.rmannibucau.cdi.cxf.api.CxfOutInterceptor;
import com.github.rmannibucau.cdi.cxf.api.NotSpecified;
import com.github.rmannibucau.cdi.cxf.api.Property;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;
import org.apache.xbean.recipe.ObjectRecipe;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CxfJaxWsClientConfiguration {
    private static final Logger LOGGER = Logger.getLogger(CxfJaxWsClientConfiguration.class.getName());

    private boolean lazy = false;
    private String wsdl = null;
    private QName qname;
    private Class<?> serviceClass;
    private String address;
    private String username;
    private String password;
    private Map<String, Object> properties;
    private List<Interceptor<? extends Message>> inInterceptors;
    private List<Interceptor<? extends Message>> outInterceptors;
    private List<Interceptor<? extends Message>> inFaultInterceptors;
    private List<Interceptor<? extends Message>> outFaultInterceptors;
    private List<Feature> features;

    public CxfJaxWsClientConfiguration(final CxfJaxWSClient annotation) {
        lazy = annotation.lazy();
        wsdl = annotation.wsdl();
        qname = parseQName(annotation.qname());
        address = annotation.address();
        username = annotation.username();
        password = annotation.password();

        properties = readProperties(annotation.properties());

        replaceEmptyByNull();
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

    private void replaceEmptyByNull() {
        if (address.isEmpty()) {
            address = null;
        }
        if (username.isEmpty()) {
            username = null;
        }
        if (password.isEmpty()) {
            password = null;
        }
    }

    private QName parseQName(final String qname) {
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

    public boolean isLazy() {
        return lazy;
    }

    public String getWsdl() {
        return wsdl;
    }

    public QName getQname() {
        return qname;
    }

    public Class<?> getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(final Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }

    public String getAddress() {
        return address;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void addOutInterceptor(final CxfOutInterceptor interceptor) {
        if (outInterceptors == null) {
            outInterceptors = new ArrayList<Interceptor<? extends Message>>();
        }

        outInterceptors.add((Interceptor<? extends Message>) instantiate(interceptor.clazz(), interceptor.classname(),
                interceptor.properties(), interceptor.propertyFile(), interceptor.prefix()));
    }

    public void addInInterceptor(final CxfInInterceptor interceptor) {
        if (inInterceptors == null) {
            inInterceptors = new ArrayList<Interceptor<? extends Message>>();
        }

        inInterceptors.add((Interceptor<? extends Message>) instantiate(interceptor.clazz(), interceptor.classname(),
                    interceptor.properties(), interceptor.propertyFile(), interceptor.prefix()));
    }

    public void addInFaultInterceptor(final CxfInFaultInterceptor interceptor) {
        if (inFaultInterceptors == null) {
            inFaultInterceptors = new ArrayList<Interceptor<? extends Message>>();
        }

        inFaultInterceptors.add((Interceptor<? extends Message>) instantiate(interceptor.clazz(), interceptor.classname(),
                interceptor.properties(), interceptor.propertyFile(), interceptor.prefix()));
    }

    public void addOutFaultInterceptor(final CxfOutFaultInterceptor interceptor) {
        if (outFaultInterceptors == null) {
            outFaultInterceptors = new ArrayList<Interceptor<? extends Message>>();
        }

        outFaultInterceptors.add((Interceptor<? extends Message>) instantiate(interceptor.clazz(), interceptor.classname(),
                interceptor.properties(), interceptor.propertyFile(), interceptor.prefix()));
    }

    public void addFeature(final CxfFeature feature) {
        if (features == null) {
            features = new ArrayList<Feature>();
        }

        features.add((Feature) instantiate(feature.clazz(), feature.classname(),
                feature.properties(), feature.propertyFile(), feature.prefix()));
    }

    public List<Interceptor<? extends Message>> getInInterceptors() {
        return inInterceptors;
    }

    public List<Interceptor<? extends Message>> getOutInterceptors() {
        return outInterceptors;
    }

    public List<Interceptor<? extends Message>> getInFaultInterceptors() {
        return inFaultInterceptors;
    }

    public List<Interceptor<? extends Message>> getOutFaultInterceptors() {
        return outFaultInterceptors;
    }

    public List<? extends Feature> getFeatures() {
        return features;
    }

    private static Object instantiate(final Class<?> inClazz, final String alternativeClassName, final Property[] properties, final String propPath, final String prefix) {
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

            return new ObjectRecipe(clazz, attributes).create();
        }

        throw new IllegalArgumentException("Can't instantiate " + alternativeClassName);
    }
}
