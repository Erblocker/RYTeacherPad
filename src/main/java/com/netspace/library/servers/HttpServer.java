package com.netspace.library.servers;

import com.netspace.library.consts.Const;
import java.io.IOException;

public class HttpServer extends NanoHTTPD {
    public HttpServer() throws IOException {
        super(Const.HTTP_PORT);
    }
}
