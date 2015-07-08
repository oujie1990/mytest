package com.hztech.tools.basic;

public class StringUtils {
	public static boolean isNull(String str){
		return str == null;
	}
	
	public static boolean isEmpty(String str){
		return isNull(str)|| "".equals(str.trim());
	}
	
	public static boolean notNull(String str){
		return !isNull(str);
	}
	public static boolean notEmpty(String str){
		return !isEmpty(str);
	}
	
	public static String getDefault(String str,String def){
		return isEmpty(str) ? def : str;
	}
}
