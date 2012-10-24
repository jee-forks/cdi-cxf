package com.github.rmannibucau.cdi.cxf;

import com.github.rmannibucau.cdi.cxf.api.CxfInInterceptor;
import com.github.rmannibucau.cdi.cxf.api.CxfInInterceptors;
import com.github.rmannibucau.cdi.cxf.api.CxfJaxWSClient;
import com.github.rmannibucau.cdi.cxf.api.Property;
import com.github.rmannibucau.cdi.cxf.bean.MyWebService;
import com.github.rmannibucau.cdi.cxf.bean.MyWebService2;
import com.github.rmannibucau.cdi.cxf.impl.CxfClientProducer;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.message.Message;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.lang.reflect.Proxy;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class CxfExtensionTest {
    @Deployment
    public static JavaArchive jar() {
        return ShrinkWrap.create(JavaArchive.class, "cdi-cxf.jar")
                    .addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"))

                    // extension
                    .addPackage(CxfClientProducer.class.getPackage())
                    .addPackage(CxfJaxWSClient.class.getPackage())

                    // test beans
                    .addPackage(MyWebService.class.getPackage());
    }

    @Inject
    @CxfInInterceptors({ @CxfInInterceptor(clazz = LoggingInInterceptor.class) })
    @CxfJaxWSClient(wsdl = "http://127.0.0.1:4204/cdi-cxf/MyWebServiceImpl?wsdl",
            qname = "{http://bean.cxf.cdi.rmannibucau.github.com/}MyWebServiceImplService")
    private MyWebService service;

    @Inject
    @CxfJaxWSClient(wsdl = "http://127.0.0.1:4204/cdi-cxf/MyWebServiceImpl2?wsdl",
            qname = "{http://bean.cxf.cdi.rmannibucau.github.com/}MyWebServiceImpl2Service")
    private MyWebService2 service2;

    @Inject
    private AnotherBeanWithSameInjection other;

    @Test
    public void validInjection() {
        assertNotNull(service);
        assertEquals("hi", service.hi());

        other.valid();

        assertNotNull(service2);
        assertEquals("hello", service2.hello());
    }

    @Test
    public void checkInterceptor() {
        final List<Interceptor<? extends Message>> inInterceptors = ((CxfClientProducer.CxfClientHandler) Proxy.getInvocationHandler(service)).getClient().getInInterceptors();
        assertEquals(1, inInterceptors.size());
        assertThat(inInterceptors.iterator().next(), instanceOf(LoggingInInterceptor.class));
    }

    public static class AnotherBeanWithSameInjection {
        @Inject
        @CxfJaxWSClient(wsdl = "http://127.0.0.1:4204/cdi-cxf/MyWebServiceImpl?wsdl",
                qname = "{http://bean.cxf.cdi.rmannibucau.github.com/}MyWebServiceImplService",
                lazy = true)
        private MyWebService service;

        public void valid() {
            assertNotNull(service);
            assertEquals("hi", service.hi());
        }
    }
}
