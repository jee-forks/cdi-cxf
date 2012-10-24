package com.github.rmannibucau.cdi.cxf.bean;

import javax.ejb.Singleton;
import javax.jws.WebService;

@Singleton
@WebService
public class MyWebServiceImpl implements MyWebService {
    @Override
    public String hi() {
        return "hi";
    }
}
