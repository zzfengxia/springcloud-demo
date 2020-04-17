package com.zz.sccommon.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 加密解密
 * 
 * @author 侯方波
 * 
 */
public class EncodeUtil {

	public static String formatStringNum(String paramString) {
		int i = paramString.indexOf('.');
		if (i == -1) {
			return paramString + ".0";
		}
		String str1 = paramString.substring(0, i);
		if (paramString.length() - i - 1 >= 2) {
			String str2 = paramString.substring(i + 1, i + 3);
			return str1 + "." + str2;
		}
		return paramString;
	}

	public static byte[] decodeBCDForBytes(byte[] paramArrayOfByte) {
		byte[] arrayOfByte1 = paramArrayOfByte;
		byte[] arrayOfByte2 = new byte[paramArrayOfByte.length * 2];
		int i = 0;
		int j = 0;
		for (int k = 0; k < arrayOfByte1.length; k++) {
			i = arrayOfByte1[k];
			j = (i & 0xF0) >> 4;
			arrayOfByte2[(2 * k)] = ((byte) j);
			j = i & 0xF;
			arrayOfByte2[(2 * k + 1)] = ((byte) j);
			if (j == 15) {
				byte[] arrayOfByte3 = new byte[2 * k + 1];
				System.arraycopy(arrayOfByte2, 0, arrayOfByte3, 0, 2 * k + 1);
				return arrayOfByte3;
			}
		}
		return arrayOfByte2;
	}

	/**
	 * @deprecated
	 */
	public static byte[] encodeBCDForBytes(byte[] paramArrayOfByte) {
		byte[] arrayOfByte;
		if (paramArrayOfByte.length % 2 == 1) {
			arrayOfByte = new byte[(paramArrayOfByte.length + 1) / 2];
		} else {
			arrayOfByte = new byte[paramArrayOfByte.length / 2];
		}
		int i = 0;
		int j = 0;
		for (int m = 0; m < paramArrayOfByte.length; m++) {
			i = paramArrayOfByte[m];
			int k = m % 2;
			if (k == 0) {
				i <<= 4;
				j = i & 0xF0;
			} else {
				j |= i;
				arrayOfByte[(m / 2)] = ((byte) j);
			}
		}
		if (paramArrayOfByte.length % 2 == 1) {
			i |= 0xF;
			arrayOfByte[((paramArrayOfByte.length + 1) / 2 - 1)] = ((byte) i);
			return arrayOfByte;
		}
		return arrayOfByte;
	}

	public static String fillLeftZero(String paramString, int paramInt) {
		if ((paramString == null) || (paramString.length() >= paramInt)) {
			return paramString;
		}
		int i = paramInt - paramString.length();
		StringBuffer localStringBuffer = new StringBuffer();
		for (int j = 0; j < i; j++) {
			localStringBuffer.append("0");
		}
		localStringBuffer.append(paramString);
		return localStringBuffer.toString();
	}

	/**
	 * @deprecated
	 */
	public static byte[] decodeBCDForStringBytes(byte[] paramArrayOfByte) {
		byte[] arrayOfByte1 = paramArrayOfByte;
		byte[] arrayOfByte2 = new byte[paramArrayOfByte.length * 2];
		int i = 0;
		int j = 0;
		for (int k = 0; k < arrayOfByte1.length; k++) {
			i = arrayOfByte1[k];
			j = (i & 0xF0) >> 4;
			arrayOfByte2[(2 * k)] = ((byte) (j + 48));
			j = i & 0xF;
			arrayOfByte2[(2 * k + 1)] = ((byte) (j + 48));
			if (j == 15) {
				byte[] arrayOfByte3 = new byte[2 * k + 1];
				System.arraycopy(arrayOfByte2, 0, arrayOfByte3, 0, 2 * k + 1);
				return arrayOfByte3;
			}
		}
		return arrayOfByte2;
	}

	/**
	 * @deprecated
	 */
	public String decodeBCD(byte[] paramArrayOfByte) {
		byte[] arrayOfByte1 = paramArrayOfByte;
		byte[] arrayOfByte2 = new byte[paramArrayOfByte.length * 2];
		int i = 0;
		int j = 0;
		for (int k = 0; k < arrayOfByte1.length; k++) {
			i = arrayOfByte1[k];
			j = (i & 0xF0) >> 4;
			arrayOfByte2[(2 * k)] = ((byte) (j + 48));
			j = i & 0xF;
			arrayOfByte2[(2 * k + 1)] = ((byte) (j + 48));
			if (j == 15) {
				byte[] arrayOfByte3 = new byte[2 * k + 1];
				System.arraycopy(arrayOfByte2, 0, arrayOfByte3, 0, 2 * k + 1);
				return new String(arrayOfByte3);
			}
		}
		return new String(arrayOfByte2);
	}

	/**
	 * @deprecated
	 */
	public byte[] encodeBCD(String paramString) {
		if (paramString == null) {
			return null;
		}
		byte[] arrayOfByte2 = paramString.getBytes();
		byte[] arrayOfByte1;
		if (arrayOfByte2.length % 2 == 1) {
			arrayOfByte1 = new byte[(arrayOfByte2.length + 1) / 2];
		} else {
			arrayOfByte1 = new byte[arrayOfByte2.length / 2];
		}
		int i = 0;
		int j = 0;
		for (int m = 0; m < arrayOfByte2.length; m++) {
			i = arrayOfByte2[m] - 48;
			if (i > 15) {
				return null;
			}
			int k = m % 2;
			if (k == 0) {
				i <<= 4;
				j = i & 0xF0;
			} else {
				j |= i;
				arrayOfByte1[(m / 2)] = ((byte) j);
			}
		}
		if (arrayOfByte2.length % 2 == 1) {
			i |= 0xF;
			arrayOfByte1[((arrayOfByte2.length + 1) / 2 - 1)] = ((byte) i);
			return arrayOfByte1;
		}
		return arrayOfByte1;
	}

	/**
	 * @deprecated
	 */
	public static byte[] encodeBCDForStringBytes(byte[] paramArrayOfByte) {
		byte[] arrayOfByte;
		if (paramArrayOfByte.length % 2 == 1) {
			arrayOfByte = new byte[(paramArrayOfByte.length + 1) / 2];
		} else {
			arrayOfByte = new byte[paramArrayOfByte.length / 2];
		}
		int i = 0;
		int j = 0;
		for (int m = 0; m < paramArrayOfByte.length; m++) {
			i = paramArrayOfByte[m] - 48;
			if (i > 15) {
				return null;
			}
			int k = m % 2;
			if (k == 0) {
				i <<= 4;
				j = i & 0xF0;
			} else {
				j |= i;
				arrayOfByte[(m / 2)] = ((byte) j);
			}
		}
		if (paramArrayOfByte.length % 2 == 1) {
			i |= 0xF;
			arrayOfByte[((paramArrayOfByte.length + 1) / 2 - 1)] = ((byte) i);
			return arrayOfByte;
		}
		return arrayOfByte;
	}

	/**
	 * @deprecated
	 */
	public static String getHexString(byte[] paramArrayOfByte) {
		return ByteUtil.byteArrayToHex(paramArrayOfByte);
	}

	/**
	 * @deprecated
	 */
	public static byte[] fromHexString(String paramString) throws Exception {
		return ByteUtil.hexToByteArray(paramString);
	}

	public static byte[] string2BCD(String paramString) {
		int i = paramString.length();
		byte[] arrayOfByte = new byte[i + 1 >> 1];
		for (int j = 0; j < i; j++) {
			int tmp24_23 = (j >> 1);
			byte[] tmp24_20 = arrayOfByte;
			tmp24_20[tmp24_23] = ((byte) (tmp24_20[tmp24_23] | paramString
					.charAt(j) - '0' << ((j & 0x1) == 1 ? 0 : 4)));
		}
		return arrayOfByte;
	}

	public static byte[] encodeNLength(short paramShort, byte[] paramArrayOfByte)
			throws Exception {
		if (paramShort == paramArrayOfByte.length) {
			return paramArrayOfByte;
		}
		if (paramShort < paramArrayOfByte.length) {
			throw new Exception("invalid length");
		}
		byte[] arrayOfByte = new byte[paramShort];
		System.arraycopy(paramArrayOfByte, 0, arrayOfByte, paramShort
				- paramArrayOfByte.length, paramArrayOfByte.length);
		return arrayOfByte;
	}

	public static byte[] encodeCnLength(short paramShort,
			byte[] paramArrayOfByte) {
		if (paramShort == paramArrayOfByte.length) {
			return paramArrayOfByte;
		}
		if (paramShort < paramArrayOfByte.length) {
			return null;
		}
		byte[] arrayOfByte = new byte[paramShort];
		System.arraycopy(paramArrayOfByte, 0, arrayOfByte, 0,
				paramArrayOfByte.length);
		for (int s = paramArrayOfByte.length; s < paramShort; s++) {
			arrayOfByte[s] = -1;
		}
		return arrayOfByte;
	}

	public static byte[] encodeAnLength(short paramShort,
			byte[] paramArrayOfByte) throws Exception {
		if (paramShort == paramArrayOfByte.length) {
			return paramArrayOfByte;
		}
		if (paramShort < paramArrayOfByte.length) {
			throw new Exception("invalid length");
		}
		byte[] arrayOfByte = new byte[paramShort];
		System.arraycopy(paramArrayOfByte, 0, arrayOfByte, 0,
				paramArrayOfByte.length);
		return arrayOfByte;
	}

	public static byte[] dcodeNLength(byte[] paramArrayOfByte) throws Exception {
		if (paramArrayOfByte == null) {
			throw new Exception("The data is null");
		}
		if (paramArrayOfByte[0] != 0) {
			return paramArrayOfByte;
		}
		int i = 0;
		for (int j = 0; j < paramArrayOfByte.length; j++) {
			if (paramArrayOfByte[j] == 0) {
				i++;
			}
		}
		byte[] arrayOfByte = new byte[paramArrayOfByte.length - i];
		System.arraycopy(paramArrayOfByte, i, arrayOfByte, 0,
				paramArrayOfByte.length - i);
		return arrayOfByte;
	}

	public static byte[] dcodeCnLength(byte[] paramArrayOfByte)
			throws Exception {
		if (paramArrayOfByte == null) {
			throw new Exception("The data is null");
		}
		if (paramArrayOfByte[(paramArrayOfByte.length - 1)] != -1) {
			return paramArrayOfByte;
		}
		int i = 0;
		for (int j = 0; j < paramArrayOfByte.length; j++) {
			if (paramArrayOfByte[j] != 255) {
				i++;
			}
		}
		byte[] arrayOfByte = new byte[i];
		System.arraycopy(paramArrayOfByte, 0, arrayOfByte, 0, i);
		return arrayOfByte;
	}

	public static byte[] dcodeAnLength(byte[] paramArrayOfByte)
			throws Exception {
		if (paramArrayOfByte == null) {
			throw new Exception("data is null");
		}
		if (paramArrayOfByte[(paramArrayOfByte.length - 1)] != 0) {
			return paramArrayOfByte;
		}
		int i = 0;
		for (int j = 0; j < paramArrayOfByte.length; j++) {
			if (paramArrayOfByte[j] != 0) {
				i++;
			}
		}
		byte[] arrayOfByte = new byte[i];
		System.arraycopy(paramArrayOfByte, 0, arrayOfByte, 0, i);
		return arrayOfByte;
	}

	public static String getEighthBitsStringFromByte(int b) {
		b |= 256;
		String str = Integer.toBinaryString(b);
		int len = str.length();
		return str.substring(len - 8, len);
	}

	/**
	 * 将一个8字节或者16字节数组转成64长度或128长度的二进制数组
	 * 
	 * @param byteArray
	 * @return
	 */
	public static boolean[] getBinaryArrayFromByteArray(byte[] byteArray) {
		boolean[] binary = new boolean[byteArray.length * 8 + 1];
		String str = "";

		for (int i = 0; i < byteArray.length; i++) {
			str += getEighthBitsStringFromByte(byteArray[i]);
		}

		System.out.println("binary String:" + str);
		for (int i = 0; i < str.length(); i++) {
			if (str.substring(i, i + 1).equalsIgnoreCase("1")) {
				binary[i + 1] = true;
			} else {
				binary[i + 1] = false;
			}
		}
		return binary;
	}

	/**
	 * 把中文字符串转换为十六进制Unicode编码字符串
	 */
	public static String stringToUnicode(String s) {
		String str = "";
		for (int i = 0; i < s.length(); i++) {
			int ch = (int) s.charAt(i);
			str += Integer.toHexString(ch).toUpperCase();
		}
		return str;
	}

	/**
	 * 把十六进制Unicode编码字符串转换为中文字符串
	 */
	public static String unicodeToString(String str) {
		if(str == null || str == ""){
			return str;
		}
		Pattern pattern = Pattern.compile("([0-9a-fA-F]{2})");
		Matcher matcher = pattern.matcher(str);
		char ch;
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			ch = (char) Integer.parseInt(matcher.group(1), 16);
			sb.append(ch);
		}
		return sb.toString();
	}
	
	public static void main(String[] paramArrayOfString) throws Exception {
		encodeBCDForStringBytes("4019f6A8".getBytes());
		string2BCD("14");
		System.out.println("IF:" + stringToUnicode("IF"));
		System.out.println("4946:" + unicodeToString("4946"));
		
		int num = 255;
		System.out.println("6 convert hex:" + ByteUtil.byteToHex((byte) num));
	}
}
