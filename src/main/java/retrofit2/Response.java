package retrofit2;

import javax.annotation.Nullable;
import okhttp3.Headers;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response.Builder;
import okhttp3.ResponseBody;
import org.apache.http.HttpStatus;

public final class Response<T> {
    @Nullable
    private final T body;
    @Nullable
    private final ResponseBody errorBody;
    private final okhttp3.Response rawResponse;

    public static <T> Response<T> success(@Nullable T body) {
        return success((Object) body, new Builder().code(200).message("OK").protocol(Protocol.HTTP_1_1).request(new Request.Builder().url("http://localhost/").build()).build());
    }

    public static <T> Response<T> success(@Nullable T body, Headers headers) {
        Utils.checkNotNull(headers, "headers == null");
        return success((Object) body, new Builder().code(200).message("OK").protocol(Protocol.HTTP_1_1).headers(headers).request(new Request.Builder().url("http://localhost/").build()).build());
    }

    public static <T> Response<T> success(@Nullable T body, okhttp3.Response rawResponse) {
        Utils.checkNotNull(rawResponse, "rawResponse == null");
        if (rawResponse.isSuccessful()) {
            return new Response(rawResponse, body, null);
        }
        throw new IllegalArgumentException("rawResponse must be successful response");
    }

    public static <T> Response<T> error(int code, ResponseBody body) {
        if (code >= HttpStatus.SC_BAD_REQUEST) {
            return error(body, new Builder().code(code).message("Response.error()").protocol(Protocol.HTTP_1_1).request(new Request.Builder().url("http://localhost/").build()).build());
        }
        throw new IllegalArgumentException("code < 400: " + code);
    }

    public static <T> Response<T> error(ResponseBody body, okhttp3.Response rawResponse) {
        Utils.checkNotNull(body, "body == null");
        Utils.checkNotNull(rawResponse, "rawResponse == null");
        if (!rawResponse.isSuccessful()) {
            return new Response(rawResponse, null, body);
        }
        throw new IllegalArgumentException("rawResponse should not be successful response");
    }

    private Response(okhttp3.Response rawResponse, @Nullable T body, @Nullable ResponseBody errorBody) {
        this.rawResponse = rawResponse;
        this.body = body;
        this.errorBody = errorBody;
    }

    public okhttp3.Response raw() {
        return this.rawResponse;
    }

    public int code() {
        return this.rawResponse.code();
    }

    public String message() {
        return this.rawResponse.message();
    }

    public Headers headers() {
        return this.rawResponse.headers();
    }

    public boolean isSuccessful() {
        return this.rawResponse.isSuccessful();
    }

    @Nullable
    public T body() {
        return this.body;
    }

    @Nullable
    public ResponseBody errorBody() {
        return this.errorBody;
    }

    public String toString() {
        return this.rawResponse.toString();
    }
}
