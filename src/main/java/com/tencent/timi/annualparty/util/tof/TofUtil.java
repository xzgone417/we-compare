package com.tencent.timi.annualparty.util.tof;

import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.DirectDecrypter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hhshan
 * @date 2023/12/21
 */
public class TofUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(TofUtil.class);

    static boolean identitySafeMode = true;

    public static AuthStaff parse(Map<String, String> headers, String token) {
        String[] extHeaders = new String[]{headers.get("x-rio-seq"), "", "", ""};
        if (!identitySafeMode) {
            extHeaders = new String[]{headers.get("x-rio-seq"), headers.get("staffid"), headers.get("staffname"), headers.get("x-ext-data")};
        }
        try {
            //1.验签
            boolean ok = checkSignature(token, headers.get("timestamp"), headers.get("signature"), extHeaders);
            if (ok) {
                //2.解密身份
                Map<String, Object> payload = decodeAuthorizationHeader(token, headers.get("x-tai-identity"));
                if (payload != null && !payload.isEmpty()) {
                    LOGGER.info("decodeAuthorizationHeader Result : {}", payload);
                } else {
                    LOGGER.warn("decodeAuthorizationHeader result is null");
                    return null;
                }
                return AuthStaff.getInstance(payload);
            } else {
                LOGGER.warn("Check Signature Failed");
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("checkSignature Failed", e);
            return null;
        }
    }

    //解密身份
    public static Map<String, Object> decodeAuthorizationHeader(String token, String authorizationHeader) {
        try {
            JWEDecrypter decrypted = new DirectDecrypter(token.getBytes());
            JWEObject jweObject = JWEObject.parse(authorizationHeader);
            jweObject.decrypt(decrypted);
            Map<String, Object> payload = jweObject.getPayload().toJSONObject();
            if (payload != null) {
                String expiration = payload.get("Expiration").toString();
                DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                        .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                        .appendFraction(ChronoField.MICRO_OF_SECOND, 0, 9, true)
                        .appendOffsetId()
                        .toFormatter();
                LocalDateTime expirationDate = LocalDateTime.parse(expiration, formatter);
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime before = now.minusMinutes(5);
                //校验请求头是否过期
                if (expirationDate.compareTo(before) < 0) {
                    LOGGER.warn("Request Header Expired");
                    throw new Exception("expired");
                }
            } else {
                LOGGER.warn("payload result is null ");
            }
            return payload;
        } catch (Exception e) {
            LOGGER.error("excption", e);
        }
        return null;
    }

    //校验网关签名
    public static boolean checkSignature(String key, String timestampSeconds, String signature, String[] extHeader) throws NoSuchAlgorithmException {
        if (timestampSeconds == null || timestampSeconds.isEmpty()) {
            return false;
        }
        long currentTimestamp = System.currentTimeMillis();
        long timestampMillis = Long.parseLong(timestampSeconds) * 1000;
        if (Math.abs(timestampMillis - currentTimestamp) > 180000) {
            // demo 为了正常运行，此处异常注释了，实际环境需要开启验证
            // return false;
        }
        String data = timestampSeconds + key + String.join(",", extHeader) + timestampSeconds;
        String calculatedSignature = sha256Hex(data);
        return signature.equalsIgnoreCase(calculatedSignature);
    }

    // 使用SHA256算法计算签名
    private static String sha256Hex(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(
                input.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte aHash : encodedHash) {
            String hex = Integer.toHexString(0xff & aHash);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
