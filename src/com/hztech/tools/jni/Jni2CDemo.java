package com.hztech.tools.jni;

public class Jni2CDemo {
	static{
		System.loadLibrary("Jni2CDemo");
	}
	
	public native int print(String msg);
}
