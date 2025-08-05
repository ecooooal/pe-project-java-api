import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


public class Main {
    static final Gson gson = new Gson();

    public static void main(String[] args) throws Exception {
        try {

            HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8090), 0);

            server.createContext("/ping", new Ping());
            server.createContext("/execute", new ExecuteCode());

            server.setExecutor(null);
            System.out.println("Java Executor started on port 8090");
            server.start();

        } catch (IOException e) {
            System.out.println("Error starting the server: " + e.getMessage());
        }
    }

    static class Ping implements HttpHandler{
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String mapped_json = mapJson(exchange);
            displayRequest(mapped_json);

            // This is for JSON Response
            String response = switch (exchange.getRequestMethod()) {
                case "GET" -> "{\"status\": \"You've sent a GET request.\"}";
                case "POST" -> "{\"status\": \"You've sent a POST request.\"}";
                default -> "{\"status\": \"Unknown method.\"}";
            };

            respond(exchange, 200, response);
        }
    }

    private static void displayRequest(String json){
        System.out.println("🔻 Incoming Request JSON:");
        System.out.println(json);
    }

    private static String mapJson(HttpExchange exchange){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        Map<String, Object> logData = new HashMap<>();
        logData.put("method", exchange.getRequestMethod());
        logData.put("uri", exchange.getRequestURI().toString());
        logData.put("headers", exchange.getRequestHeaders());

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            try {
                InputStream bodyStream = exchange.getRequestBody();
                String body = new String(bodyStream.readAllBytes(), StandardCharsets.UTF_8);
                logData.put("body", body);
            } catch (IOException e){
                System.out.println("Error : " + e.getMessage());
            }
        }

        return gson.toJson(logData);
    }

    static class ExecuteCode implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }


            try {
                CodeContext context = transformToGSON(exchange);

                CodeExecutionRunner runner = new CodeExecutionRunner();
                CodeResponse response = runner.run(context);

                String json = gson.toJson(response);

                respond(exchange, 200, json);

            } catch (Exception e) {
                e.printStackTrace();  // Log to Docker logs
                String error = gson.toJson(Map.of("error", e.getMessage()));
                respond(exchange, 500, error);
            }
        }
    }

    private static CodeContext transformToGSON(HttpExchange exchange) throws IOException {
        Gson gson = new Gson();

        InputStream bodyStream = exchange.getRequestBody();
        String body = new String(bodyStream.readAllBytes(), StandardCharsets.UTF_8);
        System.out.println(body);
        CodeContext context = gson.fromJson(body, CodeContext.class);
        context.assignClassNames();
        System.out.println(context);

        return context;
    }

    private static void respond(HttpExchange exchange, int status, String json) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        exchange.getResponseHeaders().set("Content-Type", "application/json");

        // Send response
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);

        System.out.println("🔸 Response Headers:");
        System.out.println(gson.toJson(exchange.getResponseHeaders()));

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

}
