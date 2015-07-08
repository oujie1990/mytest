package com.hztech.tools.basic;

public class LogUtils {
	public static void printError(Object info,Exception e){
		printError(new StringBuilder().append(info).append(":").append(e));
	}
	public static void printError(Object info){
		System.err.print(info);
	}
	
	public static void print(Object info){
		System.out.print(info);
	}
}
