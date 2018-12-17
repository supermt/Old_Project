package imusic.server.Controller.utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.tomcat.util.codec.binary.Base64;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JWTSigner {
    private final byte[] secret;

    public JWTSigner(String secret) {
        this(secret.getBytes());
    }

    public JWTSigner(byte[] secret) {
        this.secret = secret;
    }
    public String sign(Map<String, Object> claims) {
        Algorithm algorithm = Algorithm.HS256;
        List<String> segments = new ArrayList<String>();
        try {
            segments.add(encodedHeader());
            segments.add(encodedPayload(claims));
            segments.add(encodedSignature(join(segments, "."), algorithm));
        } catch (Exception e) {
            throw (e instanceof RuntimeException) ? (RuntimeException) e : new RuntimeException(e);
        }
        return join(segments, ".");
    }

    /**
     * 生成JWT的头部，包含有类型和算法两个信息（神特么RFC标准来着）
     */
    private String encodedHeader() throws UnsupportedEncodingException {
    	Algorithm algorithm = Algorithm.HS256;

        // create the header
        ObjectNode header = JsonNodeFactory.instance.objectNode();
        header.put("typ", "JWT");
        header.put("alg", algorithm.name());
        return base64UrlEncode(header.toString().getBytes("UTF-8"));
    }

    /**
     * 生成JWT的payload部分╮(╯_╰)╭
     */
    private String encodedPayload(Map<String, Object> _claims) throws Exception {
        Map<String, Object> claims = new HashMap<String, Object>(_claims);
        String payload = new ObjectMapper().writeValueAsString(claims);
        return base64UrlEncode(payload.getBytes("UTF-8"));
    }

    /**
     * Sign the header and payload
     */
    private String encodedSignature(String signingInput, Algorithm algorithm) throws Exception {
        byte[] signature = signHmac(algorithm, signingInput, secret);
        return base64UrlEncode(signature);
    }

    /**
     * Safe URL encode a byte array to a String
     */
    private String base64UrlEncode(byte[] str) {
        return new String(Base64.encodeBase64URLSafe(str));
    }

    private static byte[] signHmac(Algorithm algorithm, String msg, byte[] secret) throws Exception {
        Mac mac = Mac.getInstance(algorithm.getValue());
        mac.init(new SecretKeySpec(secret, algorithm.getValue()));
        return mac.doFinal(msg.getBytes());
    }
    private String join(List<String> input, String on) {
        int size = input.size();
        int count = 1;
        StringBuilder joined = new StringBuilder();
        for (String string : input) {
            joined.append(string);
            if (count < size) {
                joined.append(on);
            }
            count++;
        }
        return joined.toString();
    }

}
