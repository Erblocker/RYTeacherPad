package org.apache.http.impl.entity;

import org.apache.http.HttpException;
import org.apache.http.HttpMessage;
import org.apache.http.entity.ContentLengthStrategy;

@Deprecated
public class StrictContentLengthStrategy implements ContentLengthStrategy {
    public StrictContentLengthStrategy() {
        throw new RuntimeException("Stub!");
    }

    public long determineLength(HttpMessage message) throws HttpException {
        throw new RuntimeException("Stub!");
    }
}
