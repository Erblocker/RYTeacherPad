package okhttp3.internal.http;

import java.io.IOException;
import java.net.ProtocolException;
import okhttp3.Interceptor;
import okhttp3.Interceptor.Chain;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Response.Builder;
import okhttp3.internal.Util;
import okhttp3.internal.connection.RealConnection;
import okhttp3.internal.connection.StreamAllocation;
import okio.BufferedSink;
import okio.Okio;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HTTP;

public final class CallServerInterceptor implements Interceptor {
    private final boolean forWebSocket;

    public CallServerInterceptor(boolean forWebSocket) {
        this.forWebSocket = forWebSocket;
    }

    public Response intercept(Chain chain) throws IOException {
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        HttpCodec httpCodec = realChain.httpStream();
        StreamAllocation streamAllocation = realChain.streamAllocation();
        RealConnection connection = (RealConnection) realChain.connection();
        Request request = realChain.request();
        long sentRequestMillis = System.currentTimeMillis();
        httpCodec.writeRequestHeaders(request);
        Builder responseBuilder = null;
        if (HttpMethod.permitsRequestBody(request.method()) && request.body() != null) {
            if (HTTP.EXPECT_CONTINUE.equalsIgnoreCase(request.header(HTTP.EXPECT_DIRECTIVE))) {
                httpCodec.flushRequest();
                responseBuilder = httpCodec.readResponseHeaders(true);
            }
            if (responseBuilder == null) {
                BufferedSink bufferedRequestBody = Okio.buffer(httpCodec.createRequestBody(request, request.body().contentLength()));
                request.body().writeTo(bufferedRequestBody);
                bufferedRequestBody.close();
            } else if (!connection.isMultiplexed()) {
                streamAllocation.noNewStreams();
            }
        }
        httpCodec.finishRequest();
        if (responseBuilder == null) {
            responseBuilder = httpCodec.readResponseHeaders(false);
        }
        Response response = responseBuilder.request(request).handshake(streamAllocation.connection().handshake()).sentRequestAtMillis(sentRequestMillis).receivedResponseAtMillis(System.currentTimeMillis()).build();
        int code = response.code();
        if (this.forWebSocket && code == 101) {
            response = response.newBuilder().body(Util.EMPTY_RESPONSE).build();
        } else {
            response = response.newBuilder().body(httpCodec.openResponseBody(response)).build();
        }
        if ("close".equalsIgnoreCase(response.request().header(HTTP.CONN_DIRECTIVE)) || "close".equalsIgnoreCase(response.header(HTTP.CONN_DIRECTIVE))) {
            streamAllocation.noNewStreams();
        }
        if ((code != 204 && code != HttpStatus.SC_RESET_CONTENT) || response.body().contentLength() <= 0) {
            return response;
        }
        throw new ProtocolException("HTTP " + code + " had non-zero Content-Length: " + response.body().contentLength());
    }
}
