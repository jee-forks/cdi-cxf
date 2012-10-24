package com.github.rmannibucau.cdi.cxf.bean;

import javax.ejb.Singleton;
import javax.jws.WebService;

@Singleton
@WebService
public class MyWebServiceImpl2 implements MyWebService2 {
    @Override
    public String hello() {
        return "hello";
    }
}
