package com.github.rmannibucau.cdi.cxf.bean;

import javax.jws.WebService;

@WebService(serviceName = "MyWebServiceImplService")
public interface MyWebService {
    String hi();
}
