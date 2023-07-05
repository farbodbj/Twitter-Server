package Server.Utils.Http;



import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.twitter.common.API.ResponseMessage;
import com.twitter.common.API.ResponseModel;
import com.twitter.common.Utils.GsonUtils;
import org.apache.commons.lang3.SerializationUtils;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.twitter.common.API.StatusCode.*;
import static com.twitter.common.Utils.SafeCall.safe;


public class ServerHttpUtils {
    private final static Gson gson = GsonUtils.getInstance();

    public static boolean validateEssentialKeys(HttpExchange exchange, Map<String, String> header, String... keys) {
        for (String key: keys) {
            if(!header.containsKey(key) || header.get(key).isBlank()){
                badRequest(exchange);
                return false;
            }
        }
        return true;
    }

    public static boolean validateMethod(String method, HttpExchange exchange) {
        if (!method.equals(exchange.getRequestMethod())) {
            response(
                    exchange,
                    ResponseMessage.METHOD_NOT_ALLOWED,
                null,
                    NOT_ALLOWED,
             false);

            return false;
        }
        return true;
    }


    public static <T> T validateBody(HttpExchange exchange, Class<T> cls) {
        T body = parse(exchange, cls);
        if (body == null) {
            badRequest(exchange);
        }
        return body;

    }

    public static <T> T validateSerializedBody(HttpExchange exchange, Class<T> cls) {
        T body = parseSerialized(exchange, cls);
        if(body == null) {
            badRequest(exchange);
        }
        return body;
    }


    public static String getValueFromHeader(HttpExchange exchange, String key) {
        return exchange.getRequestHeaders().getFirst(key);
    }


    public static <T> T parse(HttpExchange exchange, Class<T> cls) {
        try(InputStreamReader inputStreamReader = new InputStreamReader(exchange.getRequestBody())){
         return gson.fromJson(
            inputStreamReader,
            cls);
        } catch (IOException e) {
            System.out.println(ResponseMessage.PARSE_ERROR);
            return null;
        }
    }


    private static <T> T parseSerialized(HttpExchange exchange, Class<T> cls) {
        try (InputStream inputStream = exchange.getRequestBody()) {
            byte[] requestBody = inputStream.readAllBytes();
            return SerializationUtils.deserialize(requestBody);
        } catch(IOException e) {
            System.out.println(ResponseMessage.PARSE_ERROR);
            return null;
        }
    }

    public static void badRequest(HttpExchange exchange) {
        response(exchange, ResponseMessage.BAD_REQUEST, null, BAD_REQUEST, false);
    }

    public static void internalServerError(HttpExchange exchange) {
        response(exchange, ResponseMessage.INTERNAL_SERVER_ERROR, null, UNKNOWN_ERROR, false);
    }

    public static void sendErrorResponse(HttpExchange exchange, String message, int statusCode) {
        response(exchange, message, null, statusCode, false);
    }

    public static <T> void sendSuccessResponse(HttpExchange exchange, String message, T res) {
        response(exchange, message, res, SUCCESS, true);
    }

    public static <T> void response(
            HttpExchange exchange,
            String message,
            T res,
            int code,
            boolean success
    ) {
        try (OutputStream os = exchange.getResponseBody()) {
            String response = gson.toJson(
                    new ResponseModel<>(code, success, message, res)
            );

            exchange.sendResponseHeaders(code, response.getBytes().length);

            os.write(response.getBytes());
        } catch (IOException e) {
            System.out.println("failed to send response");
        }

    }


    public static <T extends Serializable> void serializableResponse(
            HttpExchange exchange,
            String message,
            T res,
            int code,
            boolean success
    ) {
        try (OutputStream os = exchange.getResponseBody()) {
            ResponseModel<T> response = new ResponseModel<>(code, success, message, res);
            byte[] responseBytes = SerializationUtils.serialize((Serializable) response);
            exchange.sendResponseHeaders(code, responseBytes.length);

            os.write(responseBytes);
        } catch (IOException e) {
            System.out.println("failed to send response");
        }
    }

    public static Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null) {
            return result;
        }

        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                safe(() -> entry[1] = URLDecoder.decode(entry[1], StandardCharsets.UTF_8));
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }
}