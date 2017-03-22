package com.jso.tagit2.sync;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

/**
 * Created by jshelley on 22/03/2017.
 */

public class HttpTransportFactory {
    public static HttpTransportFactory _this;

    public static HttpTransportFactory getInstance() {
        if (_this == null)
            _this = new HttpTransportFactory();

        return _this;
    }

    private HttpTransport transport;

    public HttpTransport getTransport() {
        if (transport == null)
            transport = new NetHttpTransport();

        return transport;
    }
}
