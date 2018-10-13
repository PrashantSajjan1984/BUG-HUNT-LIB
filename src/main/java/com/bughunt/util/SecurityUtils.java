package com.bughunt.util;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;

public class SecurityUtils {
	
	 public static String hmacEncode(String algorithm, String input, String privateKey)
	        throws IllegalArgumentException {
        try {
            byte[] keyBytes = privateKey.getBytes();
            Key key = new SecretKeySpec(keyBytes, 0, keyBytes.length, algorithm);
            Mac mac = Mac.getInstance(algorithm);
            mac.init(key);
            
            return byteArrayToHex(mac.doFinal(input.getBytes()));
        } catch(NoSuchAlgorithmException ex) {
        		throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
        } catch(InvalidKeyException ex) {
        		throw new IllegalArgumentException("Illegal key: " + privateKey);
        }
	 }
	 
	 protected static String byteArrayToHex(byte [] bytes) {
	    	int hn, ln, cx;
	    	String hexDigitChars = "0123456789abcdef";
	    StringBuffer buf = new StringBuffer(bytes.length * 2);
        for(cx = 0; cx < bytes.length; cx++) {
        		hn = ((int)(bytes[cx]) & 0x00ff) / 16;
            ln = ((int)(bytes[cx]) & 0x000f);
            buf.append(hexDigitChars.charAt(hn));
            buf.append(hexDigitChars.charAt(ln));
        }
	    return buf.toString();
	 }
	 
	 public static String HMAC_MD5_encode(String key, String message) throws Exception {
		SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(),"HmacMD5");
		Mac mac = Mac.getInstance("HmacMD5");
		mac.init(keySpec);
		byte[] rawHmac = mac.doFinal(message.getBytes());
		return new String(Hex.encodeHex(rawHmac));
	}	
}
