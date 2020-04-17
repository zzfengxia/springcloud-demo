package com.zz.sccommon.util.sign;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.Map;

public class SortUtil {
	/**
	 * 对JsonObject里的每一个参数值ASCII码的增序排序，若遇到相同首字母，则看第二个字母，以此类推。
	 * 排序完成之后，再把所有“Key=Value”的形式以“&”字符连接起来
	 * @param jsonObject 未排序之前的jsonObject
	 * @return
	 */
	public static String getSortString(JsonObject jsonObject) {
		return converToString(sortString(jsonObject));
	}
	
	/**
	 * 对map里的每一个参数值ASCII码的增序排序，若遇到相同首字母，则看第二个字母，以此类推。
	 * 排序完成之后，再把所有“Key=Value”的形式以“&”字符连接起来
	 * @param map 未排序之前的map
	 * @return
	 */
	public static String getSortString(Map<String, Object> map) {
		return converToString(sortString(map));
	}
	
	/**
	 * 对数组里的每一个值ASCII码的增序排序，若遇到相同首字母，则看第二个字母，以此类推。
	 * 排序完成之后，再把所有数组值以“&”字符连接起来
	 * @param srcStrArray 未排序之前的数组
	 * @return
	 */
	public static String getSortString(String[] srcStrArray) {
		return converToString(sortString(srcStrArray));
	}
	
	/**
	 * 按照JSONObject的Key以ASCII码增序排序，若遇到相同首字母，则看第二个字母，以此类推。再以"Key=Value"的形式封装到数组中
	 * @param jsonObject
	 * @return
	 */
	private static String[] sortString(JsonObject jsonObject) {
		String[] source = new String[]{};
		// 遍历Map集合
		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			JsonElement jsonElement = entry.getValue();
			if(jsonElement != null && !jsonElement.equals("")
					&& !jsonElement.isJsonNull()){
				source = Arrays.copyOf(source, source.length + 1);
				if(jsonElement.isJsonArray() || jsonElement.isJsonObject()) {
					source[source.length - 1] = entry.getKey() + "=" + entry.getValue();
				} else {
					source[source.length - 1] = entry.getKey() + "=" + entry.getValue().getAsString();
				}
			}
		}
		
		return sortString(source);
	}
	
	/**
	 * 按照map的Key以ASCII码增序排序，若遇到相同首字母，则看第二个字母，以此类推。再以"Key=Value"的形式封装到数组中
	 * @param map
	 * @return
	 */
	private static String[] sortString(Map<String, Object> map) {
		String[] source = new String[] {};
		// 遍历Map集合
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if(entry.getValue() != null && !entry.getValue().equals("")){
				source = Arrays.copyOf(source, source.length + 1);
				source[source.length - 1] = entry.getKey() + "=" + entry.getValue();
			}
		}
		
		return sortString(source);
	}
	
	/**
	 * 按照数组值ASCII码的增序排序，若遇到相同首字母，则看第二个字母，以此类推。
	 * @param source
	 * @return
	 */
	private static String[] sortString(String[] source) {
		String str1 = "";
		String str2 = "";
		String temp = "";
		int length = 0;
		for (int i = 0; i < source.length; i++) {
			for (int m = 0; m < source.length - 1; m++) {
				str1 = source[m];
				str2 = source[m + 1];
				length = str1.length() > str2.length() ? str2.length() : str1
						.length();
				for (int j = 0; j < length; j++) {
					if (str1.charAt(j) == str2.charAt(j)) {
						continue;
					} else if (str1.charAt(j) < str2.charAt(j)) {
						break;
					} else {
						temp = str1;
						source[m] = str2;
						source[m + 1] = temp;
					}
				}
			}

		}

		return source;
	}

	/**
	 * 对排序后的字符串以“&”进行拼接
	 * @param source 排序后的数组
	 * @return
	 */
	private static String converToString(String[] source) {
		StringBuffer sb = new StringBuffer();
		String str1 = "";
		for (int k = 0; k < source.length; k++) {
			str1 = source[k];
			if (k != source.length - 1) {
				sb.append(str1).append("&");
			} else {
				sb.append(str1);
			}
		}
		return sb.toString();
	}

}