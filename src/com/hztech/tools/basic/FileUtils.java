package com.hztech.tools.basic;

import static com.hztech.tools.basic.LogUtils.*;
import static com.hztech.tools.basic.StringUtils.*;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.MessageFormat;

public class FileUtils {
	
	public static boolean existFile(String filePath){
		if (isEmpty(filePath)) {
			return false;
		}
		File file = new File(filePath);
		return file.exists() && file.isFile(); 
	}
	
	/**
	 * 新建父节点目录
	 * @param filePath
	 *            目录
	 * @return 返回目录创建后的路径
	 */
	public static void createFolder(String filePath) {
		try {
			File parentFile = new File(filePath).getParentFile();
			if (!parentFile.exists()) {
				parentFile.mkdirs();
			}
		} catch (Exception e) {
			printError("创建目录操作出错", e);
		}
	}
	
	/**
	 * 复制单个文件
	 * 
	 * @param oldFile
	 *            准备复制的文件源
	 * @param newFile
	 *            拷贝到新绝对路径带文件名
	 * @return
	 */
	public static void copyFile(String oldFile, String newFile) {
		if (!existFile(oldFile)) {
			return;
		}
		
		createFolder(newFile);
		
		FileInputStream fi = null;
		FileOutputStream fo = null;
		FileChannel in = null;
		FileChannel out = null;
		try {
			fi = new FileInputStream(oldFile);
			fo = new FileOutputStream(newFile);
			in = fi.getChannel();
			out = fo.getChannel();
			in.transferTo(0, in.size(), out);
			print(MessageFormat.format("COPY {0} TO {1} : SUCCESS", oldFile,newFile));
		} catch (Exception e) {
			printError(MessageFormat.format("COPY {0} TO {1} : ERROR >>> {2}", oldFile, newFile, e.getMessage()));
		} finally {
			closeIO(fi);
			closeIO(in);
			closeIO(fo);
			closeIO(out);
		}
	}

	/**
	 * TODO 关闭数据流
	 * oujie 2015-4-20 下午6:44:46
	 * @param io
	 */
	public static void closeIO(Closeable io) {
		if (io != null) {
			try {
				io.close();
			} catch (IOException e) {
				printError("关闭失败", e);
			}
		}
	}
}
