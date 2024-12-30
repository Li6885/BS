import entities.*;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import queries.*;
import utils.*;

import java.security.GeneralSecurityException;
import java.util.*;
import java.sql.*;

import com.sun.mail.util.MailSSLSocketFactory;
import entities.PriceDropAlert;

import javax.mail.*;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URLConnection;

public class ShoppingPriceSystemImpl implements ShoppingPriceSystem {

    private final DatabaseConnector connector;

    public ShoppingPriceSystemImpl(DatabaseConnector connector) {
        this.connector = connector;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return  sb.toString();
    }

    private static JSONObject getRequestFromUrl(String url) throws IOException, JSONException {
        URL realUrl = new URL(url);
        URLConnection conn = realUrl.openConnection();
        InputStream instream = conn.getInputStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(instream, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            instream.close();
        }
    }

    /* 用户相关接口实现 */
    @Override
    public ApiResult registerUser(Guest guest) throws SQLException {
        String sql_check_name = "SELECT * FROM shoppingUser WHERE username = ?";
        String sql_check_email = "SELECT * FROM shoppingUser WHERE email = ?";
        String sql_insert = "INSERT INTO shoppingUser (username, password, email) VALUES (?, ?, ?)";
        Connection conn = null;

        try {
            conn = connector.getConn();
            PreparedStatement pstmt_check = conn.prepareStatement(sql_check_name);
            pstmt_check.setString(1, guest.getUsername());
            ResultSet resultSet = pstmt_check.executeQuery();
            if (resultSet.next()) {
                return new ApiResult(false, "该用户名已被注册");
            }

            pstmt_check = conn.prepareStatement(sql_check_email);
            pstmt_check.setString(1, guest.getEmail());
            resultSet = pstmt_check.executeQuery();
            if (resultSet.next()) {
                return new ApiResult(false, "该邮箱已被注册");
            }

            PreparedStatement pstmt_insert = conn.prepareStatement(sql_insert);
            pstmt_insert.setString(1, guest.getUsername());
            pstmt_insert.setString(2, guest.getPassword());
            pstmt_insert.setString(3, guest.getEmail());

            int cnt = pstmt_insert.executeUpdate();
            if (cnt > 0) {
                conn.commit();
                return new ApiResult(true, "注册成功");
            } else {
                conn.rollback();
                return new ApiResult(false, "注册失败");
            }
        } catch (SQLException e) {
            conn.rollback();
            return new ApiResult(false, "数据库操作异常");
        }
    }

    @Override
    public ApiResult loginUser(Guest guest) throws SQLException {
        String sql = "SELECT email FROM shoppingUser WHERE username = ? AND password = ?";
        String checkSessionSql = "SELECT * FROM session WHERE username = ?";
        String insertSessionSql = "INSERT INTO session (username) VALUES (?)";
        Connection conn = null;
        try {
            conn = connector.getConn();
            PreparedStatement pstmt_check = conn.prepareStatement(sql);
            pstmt_check.setString(1, guest.getUsername());
            pstmt_check.setString(2, guest.getPassword());
            ResultSet resultSet = pstmt_check.executeQuery();

            if (!resultSet.next()) {// 用户名和密码不匹配
                return new ApiResult(false, "用户名或密码错误");
            }
            String email = resultSet.getString("email");

            // 2. 检查是否已经有会话
            PreparedStatement sessionStmt = conn.prepareStatement(checkSessionSql);
            sessionStmt.setString(1, guest.getUsername());
            ResultSet sessionResult = sessionStmt.executeQuery();

            if (sessionResult.next()) {// 用户已登录，存在会话
                return new ApiResult(false, "该用户已在别处登录");
            }

            // 3. 创建新的会话
            PreparedStatement insertStmt = conn.prepareStatement(insertSessionSql);
            insertStmt.setString(1, guest.getUsername());
            int rowsAffected = insertStmt.executeUpdate();
            if (rowsAffected > 0) {
                // 会话创建成功，返回成功结果
                conn.commit();
                return new ApiResult(true, "登录成功", email);
            } else {
                // 会话创建失败
                conn.rollback();
                return new ApiResult(false, "创建会话失败");
            }
        } catch (SQLException e) {
            conn.rollback();
            return new ApiResult(false, "创建会话操作异常");
        }
    }

    @Override
    public ApiResult logOutSession(String userName) throws SQLException {
        String delete = "DELETE FROM session WHERE username = ?";
        Connection conn = null;
        try {
            conn = connector.getConn();
            PreparedStatement pstmt = conn.prepareStatement(delete);
            pstmt.setString(1, userName);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // 成功删除 session
                conn.commit();
                return new ApiResult(true, "退出登录成功");
            } else {
                // 没有找到该用户的 session
                conn.rollback();
                return new ApiResult(false, "退出登录失败");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            conn.rollback();
            return new ApiResult(false, "退出登录时出现错误： " + e.getMessage());
        }
    }

    /* 搜索相关接口实现 */
    @Override
    public ApiResult queryProductPricesByPlatform(String productName, String platformName) throws SQLException {
        String sql = "SELECT product_id, platform_name, price, specification, image " +
                "FROM product " +
                "WHERE product_name = ? and platform_name = ?" +
                "ORDER BY price ASC";// 获取平台所有商品的价格信息
        String sqlAll = "SELECT product_id, platform_name, price, specification, image " +
                "FROM product " +
                "WHERE product_name = ?" +
                "ORDER BY price ASC";// 获取平台所有商品的价格信息
        Connection conn;
        try {
            conn = connector.getConn();
            boolean success;
            PreparedStatement pstmt_check;
            if(platformName == null || platformName.equals("")){
                boolean success1 = searchProducts(productName, "苏宁");
                boolean success2 = searchProducts(productName,"淘宝");
                success = success1 || success2;
                pstmt_check = conn.prepareStatement(sqlAll);
                pstmt_check.setString(1, productName);
            }else {
                success = searchProducts(productName, platformName);
                pstmt_check = conn.prepareStatement(sql);
                pstmt_check.setString(1, productName);
                pstmt_check.setString(2, platformName);
            }
            if(!success){
                return new ApiResult(false, "电商平台可能有所更新，请联系学生本人");
            }

            ResultSet resultSet = pstmt_check.executeQuery();// 执行查询
            List<Product> products = new ArrayList<>();
            boolean init = false;
            while (resultSet.next()) {
                String platform_name = resultSet.getString("platform_name");
                String productId = resultSet.getString("product_id");
                double lowestPrice = resultSet.getDouble("price");
                String specification = resultSet.getString("specification");
                String image = resultSet.getString("image");
                Product product = new Product(productId, productName, platform_name, lowestPrice, specification, image);
                init = true;
                products.add(product);
            }
            if(!init) {
                return new ApiResult(false, "未找到商品：" + productName);
            }else{
                return new ApiResult(true, "查询成功", products);
            }
        } catch (SQLException e) {// 异常处理
            e.printStackTrace();
            return new ApiResult(false, "查询商品信息时发生错误：" + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* 降价提醒相关接口实现 */
    @Override
    public ApiResult setPriceDropAlert(String productId, String email, double stdPrice) throws SQLException{
        String checkQuery = "SELECT COUNT(*) FROM price_drop_alert WHERE product_id = ? AND email = ?";
        Connection conn;
        try {
            conn = connector.getConn();
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);// 检查该商品和邮箱是否已存在降价提醒记录
            checkStmt.setString(1, productId);
            checkStmt.setString(2, email);
            ResultSet rs = checkStmt.executeQuery();
            rs.next(); // 获取查询结果
            int count = rs.getInt(1); // 结果集中返回的记录数
            if (count > 0) {
                // 如果已经存在，执行更新操作
                String updateQuery = "UPDATE price_drop_alert SET price = ? WHERE product_id = ? AND email = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setDouble(1, stdPrice);
                updateStmt.setString(2, productId);  // 使用条件来查找现有记录
                updateStmt.setString(3, email);
                if(updateStmt.executeUpdate()>0){
                    conn.commit();
                    return new ApiResult(true, "降阶提醒更新成功");
                }else{
                    conn.rollback();
                    return new ApiResult(false, "降阶提醒更新失败");
                }
            } else {
                // 如果不存在，执行插入操作
                String insertQuery = "INSERT INTO price_drop_alert (product_id, email, price) VALUES (?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setString(1, productId);
                insertStmt.setString(2, email);
                insertStmt.setDouble(3, stdPrice);
                if(insertStmt.executeUpdate()>0){
                    conn.commit();
                    return new ApiResult(true, "降阶提醒设置成功");
                }else{
                    conn.rollback();
                    return new ApiResult(false, "降阶提醒设置失败");
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
            return new ApiResult(false, "更新降价提醒表时发生错误：" + e.getMessage());
        }
    }

    @Override
    public void priceDropAlert(PriceDropAlert alert, String platform, double nowPrice) {
        Properties prop = new Properties();
        prop.setProperty("mail.host", "smtp.qq.com");///设置QQ邮件服务器
        prop.setProperty("mail.transport.protocol", "smtp");///邮件发送协议
        prop.setProperty("mail.smtp.auth", "true");//需要验证用户密码
        //QQ邮箱需要设置SSL加密
        MailSSLSocketFactory sf;
        try {
            sf = new MailSSLSocketFactory();
            sf.setTrustAllHosts(true);
            prop.put("mail.smtp.ssl.enable", "true");
            prop.put("mail.smtp.ssl.socketFactory", sf);

            //使用javaMail发送邮件的5个步骤
            //1.创建定义整个应用程序所需要的环境信息的session对象
            Session session = Session.getDefaultInstance(prop, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("912317273@qq.com", "wtdoogdfpzvxbbjc");
                }
            });
            //开启session的debug模式，这样可以查看到程序发送Email的运行状态
            session.setDebug(true);
            //2.通过session得到transport对象
            Transport ts;
            ts = session.getTransport();
            //3.使用邮箱的用户名和授权码连上邮件服务器
            ts.connect("smtp.qq.com", "912317273@qq.com", "wtdoogdfpzvxbbjc");
            //4.创建邮件：写文件
            //注意需要传递session
            MimeMessage message = new MimeMessage(session);
            //指明邮件的发件人
            message.setFrom(new InternetAddress("912317273@qq.com"));
            //指明邮件的收件人
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(alert.getEmail()));
            //邮件标题
            message.setSubject("降价提醒");
            //邮件的文本内容
            message.setContent("您的商品:<br> " + alert.getProductSpecification() + "<br>在'" + platform + "'平台上降价了。" + "<br>" +
                    "目前价格为￥" + nowPrice + "。<br>" + "据设置提醒时，价格降低了￥" +
                    String.format("%.2f", alert.getPrice() - nowPrice) + "。", "text/html;charset=UTF-8");
            //5.发送邮件
            ts.sendMessage(message, message.getAllRecipients());
            //6.关闭连接
            ts.close();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    /* 更新数据库相关接口实现 */
    @Override
    public void updatePricesWithAlert() throws InterruptedException, SQLException {
        while (true) {
            // 每1h更新一次
            updatePrices();
            checkAlert();
            storePrices();
            Thread.sleep(3600000);
        }
    }

    private void updatePrices() {
        String updateCollect = "SELECT DISTINCT product_name, platform_name FROM product";
        Connection conn;
        try {
            conn = connector.getConn();
            PreparedStatement collectStmt = conn.prepareStatement(updateCollect);
            ResultSet rs = collectStmt.executeQuery();
            while (rs.next()) {
                String productName = rs.getString("product_name");
                String platformName = rs.getString("platform_name");
                searchProducts(productName, platformName);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean searchProducts(String productName, String platformName) throws IOException {
        String url = getString(productName, platformName);
        JSONObject json = getRequestFromUrl(url);
        String error = json.getString("error");
        if(Objects.equals(error, "")) {
            JSONArray items = json.getJSONObject("items").getJSONArray("item");
            List<Product> products = new ArrayList<>();
            // 解析每个商品的数据并创建 Product 对象
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                String productId = item.getString("num_iid");
                double price = item.getDouble("price");
                String specification = item.getString("title");  // title 作为 specification
                String image = item.getString("pic_url");
                Product product = new Product(productId, productName, platformName, price, specification, image);
                products.add(product);
            }
            updateProduct(products);
            return true;
        }else{
            return false;
        }
    }

    private static String getString(String productName, String platformName) {
        String url = "";
        String SNurl = "https://api-gw.onebound.cn/suning/item_search/?key=t5165806885&q=" + productName +
                "&start_price=&end_price=&page=&cat=&discount_only=&sort=&page_size=&seller_info=&nick=&ppath=&&lang=zh-CN&secret=6885a440";
        String TBurl = "https://api-gw.onebound.cn/taobao/item_search/?key=t5165806885&secret=6885a440&q=" + productName +
                "&start_price=0&end_price=0&page=1&cat=0&discount_only=&sort=&page_size=&seller_info=&nick=&ppath=&imgid=&filter=";
        if(Objects.equals(platformName, "淘宝")){
            url = TBurl;
        }else if(Objects.equals(platformName, "苏宁")){
            url = SNurl;
        }
        return url;
    }

    private void updateProduct(List<Product> products) {
        String insertCheck = "SELECT COUNT(*) FROM product WHERE product_id = ? ";
        String insertProduct = "INSERT INTO product (product_id, product_name, platform_name, price, specification, image) VALUES(?,?,?,?,?,?)";  // 向价格表插入数据
        String updateProduct = "UPDATE product SET product_name = ?, price = ?, specification = ?, image = ? WHERE product_id = ?";
        Connection conn;
        try {
            conn = connector.getConn();
            for (Product product : products) {
                PreparedStatement checkStmt = conn.prepareStatement(insertCheck);
                checkStmt.setString(1, product.getProductId());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    if (rs.getInt(1) != 0) {//搜到了update
                        PreparedStatement updateStmt = conn.prepareStatement(updateProduct);
                        updateStmt.setString(1, product.getProductName());
                        updateStmt.setDouble(2, product.getPrice());
                        updateStmt.setString(3, product.getSpecification());
                        updateStmt.setString(4, product.getImage());
                        updateStmt.setString(5, product.getProductId());
                        updateStmt.executeUpdate();
                    } else if (rs.getInt(1) == 0) {
                        PreparedStatement insertStmt = conn.prepareStatement(insertProduct);
                        insertStmt.setString(1, product.getProductId());
                        insertStmt.setString(2, product.getProductName());
                        insertStmt.setString(3, product.getPlatformName());
                        insertStmt.setDouble(4, product.getPrice());
                        insertStmt.setString(5, product.getSpecification());
                        insertStmt.setString(6, product.getImage());
                        insertStmt.executeUpdate();
                    }
                }
            }
            conn.commit();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public void checkAlert() {
        String queryList = "SELECT DISTINCT product_id FROM price_drop_alert";
        String alertList = "SELECT email, price FROM price_drop_alert where product_id = ?";
        String queryInfo = "SELECT product_name, price, platform_name, specification FROM product WHERE product_id = ?";
        String clearAlert = "DELETE FROM price_drop_alert WHERE product_id = ? and email = ?";
        Connection conn = null;
        try {
            conn = connector.getConn();
            PreparedStatement stmt = conn.prepareStatement(queryList);
            ResultSet resultSet = stmt.executeQuery();
            List<Pair<String, String>> clears = new LinkedList<Pair<String, String>>();
            while (resultSet.next()) {
                String id = resultSet.getString("product_id");
                PreparedStatement pstmtEmail = conn.prepareStatement(alertList);
                pstmtEmail.setString(1, id);
                ResultSet rsEmail = pstmtEmail.executeQuery();

                PreparedStatement pstmtInfo = conn.prepareStatement(queryInfo);
                pstmtInfo.setString(1, id);
                ResultSet rsPrice = pstmtInfo.executeQuery();
                if (rsPrice.next()) {
                    double nowPrice = rsPrice.getDouble("price");
                    String platform = rsPrice.getString("platform_name");
                    String specification = rsPrice.getString("specification");
                    while (rsEmail.next()) {
                        double stdPrice = rsEmail.getDouble("price");
                        String email = rsEmail.getString("email");
                        PriceDropAlert alert = new PriceDropAlert(specification, stdPrice, email);
                        if(stdPrice > nowPrice){
                            priceDropAlert(alert, platform, nowPrice);
                            clears.add(Pair.of(id, email));
                            PreparedStatement pstmt = conn.prepareStatement(clearAlert);
                            pstmt.setString(1, id);
                            pstmt.setString(2, email);
                            pstmt.executeUpdate();
                        }
                    }
                }
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void storePrices() throws SQLException {
        String selectQuery = "SELECT product_id, price FROM product";  // 从商品表中选择商品ID和价格
        String insertCheck = "SELECT price FROM product_history_price WHERE product_id = ? ORDER BY time DESC LIMIT 1";
        String insertQuery = "INSERT INTO product_history_price (time, product_id, price) VALUES (NOW(), ?, ?)";  // 向历史价格表插入数据
        Connection conn = null;
        try {
            conn = connector.getConn();
            PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
            ResultSet rs = selectStmt.executeQuery();
            // 循环读取商品价格，并插入到商品历史价格表
            while (rs.next()) {
                String productId = rs.getString("product_id");
                double price = rs.getDouble("price");
                PreparedStatement checkStmt = conn.prepareStatement(insertCheck);
                checkStmt.setString(1, productId);
                ResultSet rsCheck = checkStmt.executeQuery();
                if (rsCheck.next()) {
                    double lastPrice = rsCheck.getDouble("price");
                    if (price != lastPrice) {
                        // 将当前商品的价格存入商品历史价格表
                        PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                        insertStmt.setString(1, productId);
                        insertStmt.setDouble(2, price);
                        insertStmt.executeUpdate();
                    }
                }else{
                    PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                    insertStmt.setString(1, productId);
                    insertStmt.setDouble(2, price);
                    insertStmt.executeUpdate();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();  // 出现错误时回滚事务
            e.printStackTrace();
        }
    }

    @Override
    public ApiResult getPriceHistory(String productId) throws SQLException {
        String selectQuery = "SELECT time, price FROM product_history_price WHERE product_id = ? ORDER BY time ASC";  // 查询某个商品的历史价格
        Connection conn = null;
        try {
            conn = connector.getConn();

            // 查询商品历史价格
            PreparedStatement pstmt = conn.prepareStatement(selectQuery);
            pstmt.setString(1, productId);
            ResultSet rs = pstmt.executeQuery();

            List<ProductHistoryPrice> historyPrices = new LinkedList<>();
            while (rs.next()) {
                String time = rs.getString("time");
                double price = rs.getDouble("price");
                ProductHistoryPrice historyPrice = new ProductHistoryPrice(time, productId,  price);
                historyPrices.add(historyPrice);
            }

            if (historyPrices.size() > 0) {
                return new ApiResult(true, "查询历史价格成功", historyPrices);
            } else {
                return new ApiResult(false, "该商品新上架，尚未有历史记录");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new ApiResult(false, "查询商品历史价格时出现错误: " + e.getMessage());
        }
    }

    @Override
    public ApiResult resetDatabase() {
        Connection conn = connector.getConn();
        try {
            Statement stmt = conn.createStatement();
            DBInitializer initializer = connector.getConf().getType().getDbInitializer();
            //删除所有数据
            stmt.addBatch(initializer.sqlDropShoppingUser());
            stmt.addBatch(initializer.sqlDropGuest());
            stmt.addBatch(initializer.sqlDropSession());
            stmt.addBatch(initializer.sqlDropProduct());
            stmt.addBatch(initializer.sqlDropProductHistoryPrice());
            stmt.addBatch(initializer.sqlDropPriceDropAlert());
            //新建数据表
            stmt.addBatch(initializer.sqlCreateShoppingUser());
            stmt.addBatch(initializer.sqlCreateGuest());
            stmt.addBatch(initializer.sqlCreateSession());
            stmt.addBatch(initializer.sqlCreateProduct());
            stmt.addBatch(initializer.sqlCreateProductHistoryPrice());
            stmt.addBatch(initializer.sqlCreatePriceDropAlert());
            stmt.executeBatch();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void commit(Connection conn) {
        try {
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}