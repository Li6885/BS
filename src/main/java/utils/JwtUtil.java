package utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET_KEY = "your-secret-key";  // 替换为你自己的密钥
    private static final long EXPIRATION_TIME = 3600000; // 1小时的过期时间

    // 生成 JWT Token
    public static String generateToken(String username) {
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY); // 使用 HMAC256 加密算法
        return JWT.create()
                .withIssuer("auth0")  // JWT 的签发者
                .withSubject(username) // JWT 的主题
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 设置过期时间
                .sign(algorithm); // 使用算法签名
    }

    // 解析 JWT Token
    public static DecodedJWT decodeToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        return JWT.require(algorithm)
                .withIssuer("auth0")
                .build()
                .verify(token); // 验证 token 并解码
    }

    // 校验 Token 是否过期
    public static boolean isTokenExpired(DecodedJWT decodedJWT) {
        return decodedJWT.getExpiresAt().before(new Date());
    }
}
