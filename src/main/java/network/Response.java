package network;

import java.io.Serializable;

public class Response implements Serializable {
    private ResponseType type;
    private Object data;

    private Response() {}

    public static class Builder {
        private final Response response = new Response();

        public Builder type(ResponseType type) {
            response.type = type;
            return this;
        }

        public Builder data(Object data) {
            response.data = data;
            return this;
        }

        public Response build() {
            return response;
        }
    }

    public ResponseType getType() {
        return type;
    }

    public Object getData() {
        return data;
    }
}