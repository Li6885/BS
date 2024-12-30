import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import entities.*;
import queries.*;
import utils.ConnectConfig;
import utils.DatabaseConnector;
import utils.JwtUtil;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Main {
    private static final Logger log = Logger.getLogger(Main.class.getName());

    private static ShoppingPriceSystemImpl shoppingManagementSystem;

    public static void main(String[] args) throws IOException {
        //启动数据库连接
        shoppingManagementSystem = startDatabase();
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);//请修改此处
        // 添加handler
        server.createContext("/login", new LoginHandler());
        server.createContext("/register", new RegisterHandler());
        server.createContext("/search", new SearchHandler());
        server.createContext("/logout", new ExitHandler());
        server.createContext("/alert", new AlertHandler());
        server.createContext("/history", new HistoryHandler());

        server.start();// 启动服务器
        Thread thread = new Thread(() -> {
            try {
                shoppingManagementSystem.updatePricesWithAlert();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        // 启动线程
        thread.start();

        System.out.println("Server is listening on port 8000...");// 标识后端启动
    }

    private static String readJson(HttpExchange exchange) throws IOException {
        // 读取请求体
        InputStream requestBody = exchange.getRequestBody();
        // 用这个请求体（输入流）构造个buffered reader
        BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));
        StringBuilder requestBodyBuilder = new StringBuilder();// 拼接字符串
        String line;// 用来读取信息
        while ((line = reader.readLine()) != null) {// 没读完，一直读，拼到string builder里
            requestBodyBuilder.append(line);
        }
        return requestBodyBuilder.toString();
    }

    private static void respondJson(HttpExchange exchange, ApiResult apiResult) throws IOException {
        JSONObject responseJson = new JSONObject();
        if (apiResult.ok) {
            responseJson.put("success", true);
            responseJson.put("message", apiResult.message);
            sendJsonResponse(exchange, 200, responseJson);
        } else {
            responseJson.put("success", false);
            responseJson.put("message", apiResult.message);
            sendJsonResponse(exchange, 401, responseJson);
        }
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, JSONObject responseJson) throws IOException {
        System.out.println("Sending JSON response: " + responseJson.toJSONString());
        String jsonResponse = responseJson.toJSONString();
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
        }
    }

    static class LoginHandler implements HttpHandler {
        // 关键重写handle方法
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 允许所有域的请求，cors处理
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Methods", "POST, OPTIONS");
            headers.add("Access-Control-Allow-Headers", "Content-Type");
            // 解析请求的方法，看GET还是POST
            String requestMethod = exchange.getRequestMethod();
            if (requestMethod.equals("POST")) {
                // 处理POST
                try {
                    handlePostRequest(exchange);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else if(requestMethod.equals("OPTIONS")) {
                handleOptionRequest(exchange);
            }else{
                exchange.sendResponseHeaders(405, -1);
            }
        }

        private void handleOptionRequest(HttpExchange exchange) throws IOException {
            exchange.sendResponseHeaders(204, -1);
        }

        private void handlePostRequest(HttpExchange exchange) throws IOException, SQLException {
            String request = readJson(exchange);
            System.out.println("Received POST request to log in: " + request);
            // 使用 Fastjson 解析请求体 JSON
            JSONObject jsonObject = JSONObject.parseObject(request);
            String username = jsonObject.getString("username");
            String password = jsonObject.getString("password");
            // 登录检查辑
            Guest guest = new Guest(username, password, null);
            ApiResult apiResult = shoppingManagementSystem.loginUser(guest);
            // 响应
            if (apiResult.ok) {
                // 登录成功，生成 JWT token
                String token = JwtUtil.generateToken(username);
                // 返回成功信息和 token
                JSONObject responseJson = new JSONObject();
                responseJson.put("success", true);
                responseJson.put("message", apiResult.message);
                responseJson.put("email", apiResult.payload);
                responseJson.put("token", token); // 将 token 返回给客户端
                sendJsonResponse(exchange, 200, responseJson);
            } else {
                // 用户名或密码错误
                JSONObject responseJson = new JSONObject();
                responseJson.put("success", false);
                responseJson.put("message", apiResult.message);
                sendJsonResponse(exchange, 401, responseJson); // Unauthorized
            }
        }
    }

    static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Methods", "POST, OPTIONS");
            headers.add("Access-Control-Allow-Headers", "Content-Type");

            String requestMethod = exchange.getRequestMethod();
            if (requestMethod.equals("POST")) {// 处理POST
                try {
                    handlePostRequest(exchange);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else if(requestMethod.equals("OPTIONS")) {
                handleOptionRequest(exchange);
            }else{
                exchange.sendResponseHeaders(405, -1);
            }
        }

        private void handleOptionRequest(HttpExchange exchange) throws IOException {
            exchange.sendResponseHeaders(204, -1); // 预检请求返回204
        }

        private void handlePostRequest(HttpExchange exchange) throws IOException, SQLException {
            String request = readJson(exchange);
            System.out.println("Received POST request to register: " + request);
            // 使用 Fastjson 解析请求体 JSON
            JSONObject jsonObject = JSONObject.parseObject(request);
            String username = jsonObject.getString("username");
            String password = jsonObject.getString("password");
            String emial = jsonObject.getString("email");
            // 登录检查辑
            Guest guest = new Guest(username, password, emial);
            ApiResult apiResult = shoppingManagementSystem.registerUser(guest);

            respondJson(exchange, apiResult);
        }
    }

    static class SearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Methods", "POST, OPTIONS");
            headers.add("Access-Control-Allow-Headers", "Content-Type");

            String requestMethod = exchange.getRequestMethod();
            if (requestMethod.equals("POST")) {// 处理POST
                try {
                    handlePostRequest(exchange);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else if(requestMethod.equals("OPTIONS")) {
                handleOptionRequest(exchange);
            }else{
                exchange.sendResponseHeaders(405, -1);
            }
        }

        private void handleOptionRequest(HttpExchange exchange) throws IOException {
            exchange.sendResponseHeaders(204, -1); // 预检请求返回204
        }

        private void handlePostRequest(HttpExchange exchange) throws IOException, SQLException {
            String request = readJson(exchange);
            System.out.println("Received POST request to search: " + request);
            // 使用 Fastjson 解析请求体 JSON
            JSONObject jsonObject = JSONObject.parseObject(request);
            String productName = jsonObject.getString("productName");
            JSONArray platformsArray = jsonObject.getJSONArray("platforms");

            List<String> platforms = new ArrayList<>();
            if (platformsArray != null ) {// 如果选择了平台，将平台值加入到列表
                if(platformsArray.isEmpty()) {
                    platformsArray.add("淘宝");
                    platformsArray.add("苏宁");
                }
                for (int i = 0; i < platformsArray.size(); i++) {
                    platforms.add(platformsArray.getString(i));
                }
                JSONObject responseJson = new JSONObject();
                boolean init = false;
                List<Product> products = new ArrayList<>();
                String message = "";
                if(platforms.size()==1) {
                    ApiResult apiResult = shoppingManagementSystem.queryProductPricesByPlatform(productName, platforms.get(0));
                    if (apiResult != null && apiResult.ok) {
                        responseJson.put("success", true);
                        responseJson.put("message", apiResult.message);
                        init = true;
                        products.addAll( (List<Product>) (apiResult.payload) );
                    }
                    message = apiResult.message;
                }else{
                    ApiResult apiResult = shoppingManagementSystem.queryProductPricesByPlatform(productName, null);
                    if (apiResult != null && apiResult.ok) {
                        responseJson.put("success", true);
                        responseJson.put("message", apiResult.message);
                        init = true;
                        products.addAll( (List<Product>) (apiResult.payload) );
                    }
                    message = apiResult.message;
                }
                if (!init) {
                    responseJson.put("success", false);
                    responseJson.put("message", message);
                    sendJsonResponse(exchange, 401, responseJson);
                } else {
                    responseJson.put("products", products);
                    sendJsonResponse(exchange, 200, responseJson);
                }
            }
        }
    }

    static class AlertHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Methods", "POST, OPTIONS");
            headers.add("Access-Control-Allow-Headers", "Content-Type");

            String requestMethod = exchange.getRequestMethod();
            if (requestMethod.equals("POST")) {// 处理POST
                try {
                    handlePostRequest(exchange);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else if(requestMethod.equals("OPTIONS")) {
                handleOptionRequest(exchange);
            }else{
                exchange.sendResponseHeaders(405, -1);
            }
        }

        private void handleOptionRequest(HttpExchange exchange) throws IOException {
            exchange.sendResponseHeaders(204, -1); // 预检请求返回204
        }

        private void handlePostRequest(HttpExchange exchange) throws IOException, SQLException {
            String request = readJson(exchange);
            System.out.println("Received POST request to set alert: " + request);
            // 使用 Fastjson 解析请求体 JSON
            JSONObject jsonObject = JSONObject.parseObject(request);
            String productId = jsonObject.getString("productId");
            Double price = jsonObject.getDouble("price");
            String email = jsonObject.getString("email");
            // 登录检查辑
            ApiResult apiResult = shoppingManagementSystem.setPriceDropAlert(productId, email, price);
            respondJson(exchange, apiResult);
        }
    }

    static class HistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Methods", "POST, OPTIONS");
            headers.add("Access-Control-Allow-Headers", "Content-Type");

            String requestMethod = exchange.getRequestMethod();
            if (requestMethod.equals("POST")) {// 处理POST
                try {
                    handlePostRequest(exchange);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else if(requestMethod.equals("OPTIONS")) {
                handleOptionRequest(exchange);
            }else{
                exchange.sendResponseHeaders(405, -1);
            }
        }

        private void handleOptionRequest(HttpExchange exchange) throws IOException {
            exchange.sendResponseHeaders(204, -1); // 预检请求返回204
        }

        private void handlePostRequest(HttpExchange exchange) throws IOException, SQLException {
            String request = readJson(exchange);
            System.out.println("Received POST request to search for history: " + request);
            // 使用 Fastjson 解析请求体 JSON
            JSONObject jsonObject = JSONObject.parseObject(request);
            String productId = jsonObject.getString("productId");

            ApiResult apiResult = shoppingManagementSystem.getPriceHistory(productId);

            JSONObject responseJson = new JSONObject();
            if (apiResult.ok) {
                responseJson.put("success", true);
                responseJson.put("message", apiResult.message);
                responseJson.put("history", apiResult.payload);
                sendJsonResponse(exchange, 200, responseJson);
            } else {
                responseJson.put("success", false);
                responseJson.put("message", apiResult.message);
                sendJsonResponse(exchange, 401, responseJson);
            }
        }
    }

    static class ExitHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Methods", "POST, OPTIONS");
            headers.add("Access-Control-Allow-Headers", "Content-Type");

            String requestMethod = exchange.getRequestMethod();
            if (requestMethod.equals("POST")) {// 处理POST
                try {
                    handlePostRequest(exchange);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else if(requestMethod.equals("OPTIONS")) {
                handleOptionRequest(exchange);
            }else{
                exchange.sendResponseHeaders(405, -1);
            }
        }

        private void handleOptionRequest(HttpExchange exchange) throws IOException {
            exchange.sendResponseHeaders(204, -1); // 预检请求返回204
        }

        private void handlePostRequest(HttpExchange exchange) throws IOException, SQLException {
            String request = readJson(exchange);
            System.out.println("Received POST request to log out: " + request);
            // 使用 Fastjson 解析请求体 JSON
            JSONObject jsonObject = JSONObject.parseObject(request);
            String username = jsonObject.getString("username");

            ApiResult apiResult = shoppingManagementSystem.logOutSession(username);

            respondJson(exchange, apiResult);
        }
    }

    static ShoppingPriceSystemImpl startDatabase() throws IOException {
        try {
            // parse connection config from "resources/application.yaml"
            ConnectConfig conf = new ConnectConfig();
            log.info("Success to parse connect config. " + conf.toString());
            // connect to database
            DatabaseConnector connector = new DatabaseConnector(conf);
            boolean connStatus = connector.connect();
            if (!connStatus) {
                log.severe("Failed to connect database.");
                System.exit(1);
            }
            return new ShoppingPriceSystemImpl(connector);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}