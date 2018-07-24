package com.proj.proxyservice.util;

import org.apache.commons.lang.StringUtils;

public class CastUtil {
	/**
	 * 将传入字符串转换为long类型数值
	 * 
	 * @param str
	 * @return
	 */
	public static long string2long(String str) {
		if (StringUtils.isEmpty(str)) {
			throw new RuntimeException("传入转换成long类型的字符串为空");
		}
		return Long.parseLong(str);
	}
	/**
	 * 将传入字符串转换为int类型数值 如果传入空串会抛异常
	 * 
	 * @param str
	 * @return
	 */
	public static int string2int(String str) {
		if (StringUtils.isEmpty(str)) {
			throw new RuntimeException("传入转换成int类型的字符串为空");
		}
		return Integer.parseInt(str);
	}
	
	/**
	 * 将传入字符串转换为int类型数值 如果强转出现异常则返回传入的默认值
	 * 
	 * @param str
	 * @return
	 */
	public static int string2int(String str,int defaultValue) {
		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
			return defaultValue;
		}
	}
}
