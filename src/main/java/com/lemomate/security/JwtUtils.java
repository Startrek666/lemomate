package com.lemomate.security;

import com.lemomate.model.Meeting;
import com.lemomate.model.User;
import com.lemomate.model.UserRole;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;
    
    @Value("${jitsi.app.id}")
    private String jitsiAppId;
    
    @Value("${jitsi.domain}")
    private String jitsiDomain;

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }
    
    public String generateJitsiToken(User user, Meeting meeting) {
        long expirationTime = System.currentTimeMillis() + 24 * 60 * 60 * 1000; // 24小时有效期
        
        Map<String, Object> context = new HashMap<>();
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("avatar", user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
        userInfo.put("name", user.getRealName());
        userInfo.put("email", user.getEmail());
        context.put("user", userInfo);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("context", context);
        claims.put("moderator", user.getRole() == UserRole.TEAM_ADMIN || user.getRole() == UserRole.PLATFORM_ADMIN);
        claims.put("aud", "jitsi");
        claims.put("iss", jitsiAppId);
        claims.put("sub", jitsiDomain);
        claims.put("room", meeting.getRoomName());
        claims.put("exp", expirationTime / 1000); // 转换为秒
        
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}
