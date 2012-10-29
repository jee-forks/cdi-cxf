package com.github.rmannibucau.cdi.cxf.impl;

import org.apache.cxf.feature.Feature;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;

public class CxfJaxWsClientConfiguration {
    private boolean lazy = false;
    private String wsdl = null;
    private QName qname;
    private Class<?> serviceClass;
    private String address;

    private String username;
    private String password;

    private String keyStoreType;
    private String keyManagerFactoryAlgorithm;
    private String keyStorePassword;
    private String keyStoreFile;
    private String trustStoreFile;
    private String trustStoreAlgorithm;

    private Map<String, Object> properties;
    private List<Interceptor<? extends Message>> inInterceptors;
    private List<Interceptor<? extends Message>> outInterceptors;
    private List<Interceptor<? extends Message>> inFaultInterceptors;
    private List<Interceptor<? extends Message>> outFaultInterceptors;
    private List<Feature> features;

    public CxfJaxWsClientConfiguration(final boolean lazy, final String wsdl, final QName qname, final String address,
                                       final String username, final String password,
                                       final String keyStoreType, final String keyManagerFactoryAlgorithm,
                                       final String keyStorePassword, final String keyStoreFile,
                                       final String trustStoreAlgorithm, final String trustStoreFile,
                                       final Map<String, Object> properties) {
        this.lazy = lazy;
        this.wsdl = wsdl;
        this.qname = qname;
        this.address = address;
        this.username = username;
        this.password = password;
        this.keyStoreType = keyStoreType;
        this.keyManagerFactoryAlgorithm = keyManagerFactoryAlgorithm;
        this.keyStorePassword = keyStorePassword;
        this.keyStoreFile = keyStoreFile;
        this.trustStoreAlgorithm = trustStoreAlgorithm;
        this.trustStoreFile = trustStoreFile;
        this.properties = properties;

        replaceEmptyByNull();
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
        if (keyManagerFactoryAlgorithm.isEmpty()) {
            keyManagerFactoryAlgorithm = null;
        }
        if (keyStoreFile.isEmpty()) {
            keyStoreFile = null;
        }
        if (keyStoreType.isEmpty()) {
            keyStoreType = null;
        }
        if (trustStoreAlgorithm.isEmpty()) {
            trustStoreAlgorithm = null;
        }
        if (trustStoreFile.isEmpty()) {
            trustStoreFile = null;
        }
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

    public boolean hasSSL() {
        return false;
    }

    public void setInInterceptors(final List<Interceptor<? extends Message>> inInterceptors) {
        this.inInterceptors = inInterceptors;
    }

    public void setOutInterceptors(final List<Interceptor<? extends Message>> outInterceptors) {
        this.outInterceptors = outInterceptors;
    }

    public void setInFaultInterceptors(final List<Interceptor<? extends Message>> inFaultInterceptors) {
        this.inFaultInterceptors = inFaultInterceptors;
    }

    public void setOutFaultInterceptors(final List<Interceptor<? extends Message>> outFaultInterceptors) {
        this.outFaultInterceptors = outFaultInterceptors;
    }

    public void setFeatures(final List<Feature> features) {
        this.features = features;
    }

    public void addInInterceptor(final Interceptor<? extends Message> interceptor) {
        inInterceptors.add(interceptor);
    }

    public void addInFaultInterceptor(final Interceptor<? extends Message> interceptor) {
        inFaultInterceptors.add(interceptor);
    }

    public void addOutInterceptor(final Interceptor<? extends Message> interceptor) {
        outInterceptors.add(interceptor);
    }

    public void addOutFaultInterceptor(final Interceptor<? extends Message> interceptor) {
        outFaultInterceptors.add(interceptor);
    }

    public void addFeature(final Feature feature) {
        features.add(feature);
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(final String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public String getKeyManagerFactoryAlgorithm() {
        return keyManagerFactoryAlgorithm;
    }

    public void setKeyManagerFactoryAlgorithm(final String keyManagerFactoryAlgorithm) {
        this.keyManagerFactoryAlgorithm = keyManagerFactoryAlgorithm;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(final String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getKeyStoreFile() {
        return keyStoreFile;
    }

    public void setKeyStoreFile(final String keyStoreFile) {
        this.keyStoreFile = keyStoreFile;
    }

    public String getTrustStoreFile() {
        return trustStoreFile;
    }

    public void setTrustStoreFile(final String trustStoreFile) {
        this.trustStoreFile = trustStoreFile;
    }

    public String getTrustStoreAlgorithm() {
        return trustStoreAlgorithm;
    }

    public void setTrustStoreAlgorithm(final String trustStoreAlgorithm) {
        this.trustStoreAlgorithm = trustStoreAlgorithm;
    }
}
