package com.samadhan.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class TokenApi {
    private final String encryptedSecurityKey = "Vhl//LHYRuRrqv5F7qmtYdo2Fjpejt03DJ5DFXWy4oYTXIVia2TvMg==";
    private final String encryptedClaimData = "KF76XOQqJ5qLeh2h7mOCX+/Ez0e3uQwwB8GMDdp3sL0TOw18qUYrhQ==";

    private final String alg_claim_field = "alg";
    private final String jwtId = "admin";
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    public String generateToken(String userName, int tokenAliveMin) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(alg_claim_field, encryptedClaimData);
        return createToken(claims, userName, jwtId, tokenAliveMin);
    }


    public Claims extractAllClaims(String token) throws ExpiredJwtException {
        return Jwts
                .parser()
                .setSigningKey(encryptedSecurityKey)
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }


    public Boolean validateToken(final String token) {
        String alg = extractClaim(token, alg_claim_field);
        return !isTokenExpired(token) &&
                alg.equals(encryptedClaimData);
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date getIssuedTime(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) throws ExpiredJwtException {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }


    private String createToken(Map<String, Object> claims, String userName, String jtId, int tokenPeriod) {
        int aliveMin = 1000 * 60 * tokenPeriod;
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userName)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + aliveMin))
                .setId(jtId)
                .signWith(signatureAlgorithm, encryptedSecurityKey)
                .compact();
    }

    private String extractClaim(String token, String claim) {
        return extractAllClaims(token).get(claim, String.class);
    }


    public boolean verifyUserToken(String idToken) throws FirebaseAuthException {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            decodedToken.getUid();
            return true;
        } catch (Exception exp) {
            return false;
        }
    }
}
