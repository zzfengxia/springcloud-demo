package com.zz.sccommon.util.sign;

import org.apache.commons.codec.binary.Base64;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSASignatureUtil {
	/**
	 * 签名算法
	 */
	public static final String SIGN_ALGORITHMS_SHA1 = "SHA1WithRSA";
	
	public static final String SIGN_ALGORITHMS_SHA256 = "SHA256WithRSA";
	
	/**
	 * RSA签名
	 * @param content 待签名数据
	 * @param privateKey 私钥
	 * @param encode 字符编码
	 * @param signAlgorithms 签名算法，值可从本util的常量中获取
	 * @return 签名值
	 */
	public static String sign(String content, String privateKey, String encode, String signAlgorithms) {
		try {
			PKCS8EncodedKeySpec priPkcs8 = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKey));
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			
			PrivateKey priKey = keyFactory.generatePrivate(priPkcs8);
			
			Signature signature = Signature.getInstance(signAlgorithms);
			signature.initSign(priKey);
			signature.update(content.getBytes(encode));
			
			byte[] signed = signature.sign();
			
			return Base64.encodeBase64String(signed);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * RSA签名
	 * @param content 待签名数据
	 * @param privateKey 私钥
	 * @param signAlgorithms 签名算法，值可从本util的常量中获取
	 * @return 签名值
	 */
	public static String sign(String content, String privateKey, String signAlgorithms) {
		try {
			PKCS8EncodedKeySpec priPkcs8 = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKey));
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			
			PrivateKey priKey = keyFactory.generatePrivate(priPkcs8);
			
			Signature signature = Signature.getInstance(signAlgorithms);
			signature.initSign(priKey);
			signature.update(content.getBytes());
			
			byte[] signed = signature.sign();
			
			return Base64.encodeBase64String(signed);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * RSA验签名检查 
	 * @param content 待签名数据 
	 * @param sign 签名值 
	 * @param publicKey 分配给开发商公钥 
	 * @param encode 字符集编码 
	 * @param algorithm
	 * @return 验签结果
	 */
	public static boolean doCheck(String content, String sign, String publicKey, String encode, String algorithm) {
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			byte[] encodedKey = Base64.decodeBase64(publicKey);
			PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
			
			Signature signature = Signature.getInstance(algorithm);
			signature.initVerify(pubKey);
			signature.update(content.getBytes(encode));
			
			boolean isVerify = signature.verify(Base64.decodeBase64(sign));
			return isVerify;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * RSA验签名检查 
	 * @param content 待签名数据 
	 * @param sign 签名值 
	 * @param publicKey 分配给开发商公钥 
	 * @param algorithm
	 * @return 验签结果
	 */
	public static boolean doCheck(String content, String sign, String publicKey, String algorithm) {
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			byte[] encodedKey = Base64.decodeBase64(publicKey);
			PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
			
			Signature signature = Signature.getInstance(algorithm);
			signature.initVerify(pubKey);
			signature.update(content.getBytes());
			
			boolean isVerify = signature.verify(Base64.decodeBase64(sign));
			return isVerify;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
