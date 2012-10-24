package com.github.rmannibucau.cdi.cxf.bean;

import javax.jws.WebService;

@WebService(serviceName = "MyWebServiceImpl2Service")
public interface MyWebService2 {
    String hello();
}
