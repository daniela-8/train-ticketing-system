package network;

import java.io.Serializable;

public class Request implements Serializable {
    private RequestType type;
    private Object data;

    private Request() {}

    public static class Builder {
        private final Request request = new Request();

        public Builder type(RequestType type) {
            request.type = type;
            return this;
        }

        public Builder data(Object data) {
            request.data = data;
            return this;
        }

        public Request build() {
            return request;
        }
    }

    public RequestType getType() {
        return type;
    }

    public Object getData() {
        return data;
    }
}