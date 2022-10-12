package com.hmdp.utils;

import io.jsonwebtoken.*;
import org.springframework.util.StringUtils;

import java.util.Date;

import static com.hmdp.common.JwtConstant.tokenExpiration;
import static com.hmdp.common.JwtConstant.tokenSignKey;

public class JwtHelper {

    /**
     * 生成 token
     */
    public static String createToken(Long userId,String userName){
        String token = Jwts.builder()
                .setSubject("TEST_USER")
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .claim("userId",userId)
                .claim("userName",userName)
                .signWith(SignatureAlgorithm.HS512,tokenSignKey)
                .compressWith(CompressionCodecs.GZIP)
                .compact();
        return token;
    }

    public static Long getUserId(String token){
        if(StringUtils.isEmpty(token)){
            return null;
        }
        Jws<Claims> claimsJws = null;
        try {
            claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        } catch (Exception e) {
            return null;
        }
        Claims claims = claimsJws.getBody();
        return Long.parseLong(claims.get("userId").toString());
    }

    public static String getUserName(String token){
        if(StringUtils.isEmpty(token)){
            return null;
        }
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();
        return claims.get("userName").toString();
    }

    public static void main(String[] args) {
        String token = JwtHelper.createToken(1234L, "张三");
        System.out.println(token);
        System.out.println(JwtHelper.getUserId(token));
        System.out.println(JwtHelper.getUserName(token));
    }

}
