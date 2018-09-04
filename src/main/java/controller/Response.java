package controller;

/**
 * Created by kaspar on 29.04.18.
 */
public class Response<T> {

    public enum StatusCode {
        OK, TIMEOUT, ERROR
    }

    private T body;
    private StatusCode statusCode;

    public Response(T body, StatusCode statusCode) {
        this.body = body;
        this.statusCode = statusCode;
    }

    public T getBody() {
        return body;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }
}
