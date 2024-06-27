package com.arundaon.blog_api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

@Component
public class JWTProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;

    public String generateJwt(Integer id){
        return Jwts.builder()
                .subject(id.toString())
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + 1000 * 60 * 60))
                .signWith(SignatureAlgorithm.HS256,this.jwtSecret)
                .compact();
    }

    public String getUserId(String token){
        try{
            Claims payload = Jwts.parser().setSigningKey(this.jwtSecret).build().parseSignedClaims(token).getPayload();
            return payload.getSubject();
        }
        catch (JwtException jwte){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "token is Invalid or expired");
        }
    }

}
