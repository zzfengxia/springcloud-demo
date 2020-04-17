package com.zz.sccommon.util;

public class ByteUtil {
	public static int byteArrayToInt(byte[] paramArrayOfByte, int paramInt) {
		int i = 0;
		i |= 0xFF000000 & paramArrayOfByte[(paramInt + 0)] << 24;
		i |= 0xFF0000 & paramArrayOfByte[(paramInt + 1)] << 16;
		i |= 0xFF00 & paramArrayOfByte[(paramInt + 2)] << 8;
		i |= 0xFF & paramArrayOfByte[(paramInt + 3)];
		return i;
	}

	public static long byteArrayToLong(byte[] paramArrayOfByte) {
		long l = 0L;
		l |= 0x0 & paramArrayOfByte[0] << 56;
		l |= 0x0 & paramArrayOfByte[1] << 48;
		l |= 0x0 & paramArrayOfByte[2] << 40;
		l |= 0x0 & paramArrayOfByte[3] << 32;
		l |= 0xFF000000 & paramArrayOfByte[4] << 24;
		l |= 0xFF0000 & paramArrayOfByte[5] << 16;
		l |= 0xFF00 & paramArrayOfByte[6] << 8;
		l |= 0xFF & paramArrayOfByte[7];
		return l;
	}

	public static short byteArrayToShort(byte[] paramArrayOfByte, int paramInt) {
		int i = 0;
		i |= 0xFF00 & paramArrayOfByte[(paramInt + 0)] << 8;
		i |= 0xFF & paramArrayOfByte[(paramInt + 1)];
		return (short) i;
	}

	public static byte[] intToByteArray(int paramInt) {
		byte[] arrayOfByte = new byte[4];
		intToByteArray(paramInt, arrayOfByte, 0);
		return arrayOfByte;
	}

	public static int intToByteArray(int paramInt1, byte[] paramArrayOfByte,
			int paramInt2) {
		for (int i = 0; i < 4; i++) {
			paramArrayOfByte[(paramInt2 + 3 - i)] = ((byte) (paramInt1 & 0xFF));
			paramInt1 >>= 8;
		}
		return 4;
	}

	public static byte[] longToByteArray(long paramLong) {
		byte[] arrayOfByte = new byte[8];
		for (int i = 0; i < arrayOfByte.length; i++) {
			arrayOfByte[(7 - i)] = ((byte) (int) (paramLong & 0xFF));
			paramLong >>= 8;
		}
		return arrayOfByte;
	}

	public static byte[] shortToByteArray(int paramInt) {
		byte[] arrayOfByte = new byte[2];
		shortToByteArray(paramInt, arrayOfByte, 0);
		return arrayOfByte;
	}

	public static short shortToByteArray(int paramInt1,
			byte[] paramArrayOfByte, int paramInt2) {
		for (int i = 0; i < 2; i++) {
			paramArrayOfByte[(paramInt2 + 1 - i)] = ((byte) (paramInt1 & 0xFF));
			paramInt1 >>= 8;
		}
		return 2;
	}

	public static byte[] uudecode(String paramString) {
		int i = 0;
		StringBuffer localStringBuffer = new StringBuffer();
		while (paramString.charAt(i) != ';') {
			localStringBuffer.append(paramString.charAt(i));
			i++;
		}
		i++;
		int j = Integer.parseInt(localStringBuffer.toString());
		byte[] arrayOfByte1 = new byte[j];
		int[] arrayOfInt1 = new int[4];
		byte[] arrayOfByte2 = new byte[3];
		int[] arrayOfInt2 = new int[3];
		int k = 0;
		while (i < paramString.length()) {
			arrayOfInt1[0] = (paramString.charAt(i) - '2');
			arrayOfInt1[1] = (paramString.charAt(i + 1) - '2');
			arrayOfInt1[2] = (paramString.charAt(i + 2) - '2');
			arrayOfInt1[3] = (paramString.charAt(i + 3) - '2');
			arrayOfInt2[0] = (arrayOfInt1[0] << 2 | (arrayOfInt1[1] & 0x30) >> 4);
			arrayOfInt2[1] = ((arrayOfInt1[1] & 0xF) << 4 | (arrayOfInt1[2] & 0x3C) >> 2);
			arrayOfInt2[2] = ((arrayOfInt1[2] & 0x3) << 6 | arrayOfInt1[3]);
			for (int m = 0; m < 3; m++) {
				if (k < j) {
					arrayOfByte1[(k++)] = ((byte) arrayOfInt2[m]);
				}
			}
			i += 4;
		}
		return arrayOfByte1;
	}

	public static String uuencode(byte[] paramArrayOfByte) {
		StringBuffer localStringBuffer = new StringBuffer(
				paramArrayOfByte.length * 4 / 3);
		localStringBuffer.append(paramArrayOfByte.length);
		localStringBuffer.append(';');
		byte[] arrayOfByte = new byte[3];
		char[] arrayOfChar = new char[4];
		int[] arrayOfInt = new int[4];
		for (int i = 0; i < paramArrayOfByte.length; i += 3) {
			arrayOfByte[0] = paramArrayOfByte[i];
			if (i + 1 < paramArrayOfByte.length) {
				arrayOfByte[1] = paramArrayOfByte[(i + 1)];
			} else {
				arrayOfByte[1] = 32;
			}
			if (i + 2 < paramArrayOfByte.length) {
				arrayOfByte[2] = paramArrayOfByte[(i + 2)];
			} else {
				arrayOfByte[2] = 32;
			}
			arrayOfInt[0] = ((arrayOfByte[0] & 0xFC) >> 2);
			arrayOfInt[1] = ((arrayOfByte[0] & 0x3) << 4 | (arrayOfByte[1] & 0xF0) >> 4);
			arrayOfInt[2] = ((arrayOfByte[1] & 0xF) << 2 | (arrayOfByte[2] & 0xC0) >> 6);
			arrayOfInt[3] = (arrayOfByte[2] & 0x3F);
			for (int j = 0; j < 4; j++) {
				arrayOfChar[j] = ((char) (50 + arrayOfInt[j]));
			}
			localStringBuffer.append(arrayOfChar);
		}
		return localStringBuffer.toString();
	}

	public static boolean getBit(byte[] paramArrayOfByte, int paramInt)
			throws ArrayIndexOutOfBoundsException {
		return (paramArrayOfByte[(paramInt / 8)] & 1 << 7 - paramInt % 8) != 0;
	}

	public static void setBit(byte[] paramArrayOfByte, int paramInt,
			boolean paramBoolean) throws ArrayIndexOutOfBoundsException {
		int i = paramInt / 8;
		int j = 1 << 7 - paramInt % 8;
		if (paramBoolean) {
			int tmp22_21 = i;
			paramArrayOfByte[tmp22_21] = ((byte) (paramArrayOfByte[tmp22_21] | j));
		} else {
			int tmp34_33 = i;
			paramArrayOfByte[tmp34_33] = ((byte) (paramArrayOfByte[tmp34_33] & (j ^ 0xFFFFFFFF)));
		}
	}

	private static int hexDigitValue(char paramChar) throws Exception {
		int i = 0;
		if ((paramChar >= '0') && (paramChar <= '9')) {
			i = (byte) paramChar - 48;
		} else if ((paramChar >= 'A') && (paramChar <= 'F')) {
			i = (byte) paramChar - 55;
		} else if ((paramChar >= 'a') && (paramChar <= 'f')) {
			i = (byte) paramChar - 87;
		} else {
			throw new Exception();
		}
		return i;
	}

	public static byte hexToByte(String paramString) throws Exception {
		if (paramString == null) {
			throw new Exception();
		}
		if (paramString.length() != 2) {
			throw new Exception();
		}
		byte[] arrayOfByte = paramString.getBytes();
		byte b = (byte) (hexDigitValue((char) arrayOfByte[0]) * 16 + hexDigitValue((char) arrayOfByte[1]));
		return b;
	}

	public static byte[] hexToByteArray(String paramString) throws Exception {
		if (paramString == null) {
			throw new Exception();
		}
		if (paramString.length() % 2 != 0) {
			throw new Exception();
		}
		int i = paramString.length() / 2;
		byte[] arrayOfByte = new byte[i];
		for (int j = 0; j < i; j++) {
			arrayOfByte[j] = hexToByte(paramString.substring(j * 2, j * 2 + 2));
		}
		return arrayOfByte;
	}

	public static void main(String[] paramArrayOfString) {
		byte[] arrayOfByte = { 3, 64, 65, -1 };
		System.out.println("String = " + byteArrayToHex(arrayOfByte));
	}

	public static String byteArrayToHex(byte[] paramArrayOfByte) {
		String str = "";
		if ((paramArrayOfByte == null) || (paramArrayOfByte.length == 0)) {
			return str;
		}
		for (int i = 0; i < paramArrayOfByte.length; i++) {
			int j = paramArrayOfByte[i];
			int k = j & 0xF;
			k += (k < 10 ? 48 : 55);
			int m = (j & 0xF0) >> 4;
			m += (m < 10 ? 48 : 55);
			str = str + (char) m + (char) k;
		}
		return str;
	}

	public static String byteToHex(byte paramByte) {
		int i = paramByte & 0xF;
		i += (i < 10 ? 48 : 55);
		int j = (paramByte & 0xF0) >> 4;
		j += (j < 10 ? 48 : 55);
		String str = "" + (char) j + (char) i;
		return str;
	}

	public static short byteArrayToShort(byte[] paramArrayOfByte) {
		return byteArrayToShort(paramArrayOfByte, 0);
	}

	public static boolean compareByte(byte[] paramArrayOfByte1,
			byte[] paramArrayOfByte2) {
		if ((paramArrayOfByte1 == null) || (paramArrayOfByte2 == null)) {
			return false;
		}
		if (paramArrayOfByte1.length != paramArrayOfByte2.length) {
			return false;
		}
		for (int i = 0; i < paramArrayOfByte1.length; i++) {
			if (paramArrayOfByte1[i] != paramArrayOfByte2[i]) {
				return false;
			}
		}
		return true;
	}

	public static String dateBytesToString(byte[] paramArrayOfByte)
			throws Exception {
		if ((paramArrayOfByte == null) || (paramArrayOfByte.length != 4)) {
			throw new Exception("Invalid length");
		}
		byte[] arrayOfByte = EncodeUtil
				.decodeBCDForStringBytes(paramArrayOfByte);
		return new String(arrayOfByte);
	}

	public static int byteArrayToIntLeftLeast(byte[] paramArrayOfByte,
			int paramInt) {
		int i = 0;
		i |= 0xFF & paramArrayOfByte[(paramInt + 0)];
		i |= 0xFF00 & paramArrayOfByte[(paramInt + 1)] << 8;
		i |= 0xFF0000 & paramArrayOfByte[(paramInt + 2)] << 16;
		i |= 0xFF000000 & paramArrayOfByte[(paramInt + 3)] << 24;
		return i;
	}

	public static short byteArrayToShortLeftLeast(byte[] paramArrayOfByte,
			int paramInt) {
		int i = 0;
		i |= 0xFF & paramArrayOfByte[(paramInt + 0)];
		i |= 0xFF00 & paramArrayOfByte[(paramInt + 1)] << 8;
		return (short) i;
	}

	public static byte[] intToByteArrayLeftLeast(int paramInt) {
		byte[] arrayOfByte = new byte[4];
		intToByteArrayLeftLeast(paramInt, arrayOfByte, 0);
		return arrayOfByte;
	}

	public static int intToByteArrayLeftLeast(int paramInt1,
			byte[] paramArrayOfByte, int paramInt2) {
		for (int i = 0; i < 4; i++) {
			paramArrayOfByte[(i + paramInt2)] = ((byte) (paramInt1 & 0xFF));
			paramInt1 >>= 8;
		}
		return 4;
	}

	public static byte[] shortToByteArrayLeftLeast(int paramInt) {
		byte[] arrayOfByte = new byte[2];
		shortToByteArrayLeftLeast(paramInt, arrayOfByte, 0);
		return arrayOfByte;
	}

	public static short shortToByteArrayLeftLeast(int paramInt1,
			byte[] paramArrayOfByte, int paramInt2) {
		for (int i = 0; i < 2; i++) {
			paramArrayOfByte[(i + paramInt2)] = ((byte) (paramInt1 & 0xFF));
			paramInt1 >>= 8;
		}
		return 2;
	}

	public static int byteArrayToInt(byte[] paramArrayOfByte) {
		return byteArrayToInt(paramArrayOfByte, 0);
	}

	public static byte[] stringToDateBytes(String paramString) throws Exception {
		if ((paramString == null) || (paramString.length() != 8)) {
			throw new Exception("Invalid length");
		}
		byte[] arrayOfByte1 = paramString.getBytes();
		byte[] arrayOfByte2 = EncodeUtil.encodeBCDForStringBytes(arrayOfByte1);
		return arrayOfByte2;
	}

	public static byte[] stringToTimeBytes(String paramString) throws Exception {
		if ((paramString == null) || (paramString.length() != 6)) {
			throw new Exception("Invalid length");
		}
		byte[] arrayOfByte1 = paramString.getBytes();
		byte[] arrayOfByte2 = EncodeUtil.encodeBCDForStringBytes(arrayOfByte1);
		return arrayOfByte2;
	}

	public static String timeBytesToString(byte[] paramArrayOfByte)
			throws Exception {
		if (paramArrayOfByte.length != 3) {
			throw new Exception("Invalid length");
		}
		StringBuffer localStringBuffer = new StringBuffer(new String(
				EncodeUtil.decodeBCDForStringBytes(paramArrayOfByte)));
		localStringBuffer.insert(4, ':');
		localStringBuffer.insert(2, ':');
		return localStringBuffer.toString().substring(0, 8);
	}

	public static String leftPADSpace(String paramString, int paramInt) {
		if ((paramString == null) || (paramString.equals(""))) {
			return null;
		}
		int i = paramString.length();
		if (i > paramInt) {
			return paramString.substring(0, paramInt);
		}
		char c = ' ';
		StringBuffer localStringBuffer = new StringBuffer();
		int j = paramInt - i;
		for (int k = 0; k < j; k++) {
			localStringBuffer.append(c);
		}
		localStringBuffer.append(paramString);
		return localStringBuffer.toString();
	}

	public static String leftInterceptSpace(String paramString) {
		if ((paramString == null) || (paramString.equals(""))) {
			return null;
		}
		return paramString.trim();
	}

	public static String spaceString(int paramInt) {
		char c = ' ';
		StringBuffer localStringBuffer = new StringBuffer();
		for (int i = 0; i < paramInt; i++) {
			localStringBuffer.append(c);
		}
		return localStringBuffer.toString();
	}
}
