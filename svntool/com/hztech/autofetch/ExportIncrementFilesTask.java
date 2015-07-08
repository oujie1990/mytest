package com.hztech.autofetch;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.MessageFormat;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * TODO 结合 svnant 插件，获取项目增量包
 * @author oujie
 * 2015-4-20 下午6:34:51
 */
public class ExportIncrementFilesTask extends Task {
	// WEB-INF/classes
	private String srcDir = new StringBuilder("WEB-INF").append(File.separator)
			.append("classes").toString();
	// 目标文件
	private String targetFile;
	// 目标文件目录
	private String targetDir;
	// 增量包目录
	private String incrementDir;
	// 排除不打包目录
	private String[] excludeDir;

	public void setTargetDir(String targetDir) {
		this.targetDir = targetDir;
	}

	public void setTargetFile(String targetFile) {
		this.targetFile = targetFile;
	}

	public void setIncrementDir(String incrementDir) {
		this.incrementDir = incrementDir;
	}

	public void setExcludeDir(String excludeDir) {
		if(excludeDir == null || excludeDir.trim().equals(""))
			return ;
		this.excludeDir = excludeDir.split(";");
	}

	@Override
	public void execute() throws BuildException {
		super.execute();
		checkConfig();
		runSvnText();
	}

	/**
	 * TODO 检查ant配置参数
	 * oujie 2015-4-20 下午6:38:59
	 */
	private void checkConfig() {
		if (targetDir == null || targetDir.trim().equals(""))
			throw new BuildException("targetDir attribute can't be null!");

		if (targetFile == null || targetFile.trim().equals(""))
			throw new BuildException("targetFile attribute can't be null!");

		if (incrementDir == null || incrementDir.trim().equals(""))
			throw new BuildException("incrementDir attribute can't be null!");
	}

	/**
	 * TODO 读取svn更改统计汇总文件，
	 * 将文件拷贝到指定目录下，生成项目增量包
	 * oujie 2015-4-20 下午6:39:16
	 */
	public void runSvnText() {
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(targetFile, "r");
			int count = 0, vaildCount = 0;
			for (String line; (line = raf.readLine()) != null; count++) {
				if (! isExclude(line)) {
					String filePath = line.trim();
					if (checkFile(filePath)) {
						++vaildCount;
						exportFile(filePath);
					}
				}
			}
			log(MessageFormat
					.format("========================总共提交的文件数有{0}个,有效部署文件有{1}个========================",
							count, vaildCount));
			raf.close();
		} catch (Exception e) {
			new BuildException(e);
		} finally {
			closeIO(raf);
		}
	}

	/**
	 * TODO 排除项目外文件，暂时为(.标志开头的文件或文件夹)
	 * oujie 2015-4-20 下午6:40:32
	 * oujie 2015-06-12 11:08:30 修改 排除SVN文件夹拷贝
	 * @param filePath
	 * @return
	 */
	private boolean isExclude(String filePath) {
		if (isBlank(filePath)) {
			return true;
		}
		
		String filePathTrim = filePath.trim();
		if (filePathTrim.startsWith(".")
				|| filePath.indexOf("/.") != -1) {
			// 已(.)符号开头的文件
			return true;
		}else if(isExcludeDir(filePathTrim)){
			return true;
		} else {
			return false;
		}
	}

	private boolean isExcludeDir(String filePath){
		if (excludeDir == null || excludeDir.length < 1) {
			return false;
		}
		for (String exString : excludeDir) {
			if (filePath.startsWith(exString)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * TODO 字符串判空
	 * oujie 2015-4-20 下午6:41:24
	 * @param str
	 * @return
	 */
	private static boolean isBlank(String str) {
		return str == null || str.trim().equals("");
	}

	/**
	 * TODO 导出Web类文件，就是存放在webapp下的文件，不包括源码路径下文件
	 * oujie 2015-4-20 下午6:41:43
	 * @param filePath
	 */
	public void exportWebFile(String filePath) {
		String targetFilePath = new StringBuilder(targetDir)
				.append(File.separator).append(filePath).toString();
		String incrementFilePath = new StringBuilder(incrementDir)
				.append(File.separator)
				.append(filePath.substring("context".length() + 1)).toString();
		createParentFolder(incrementFilePath);
		copyFile(targetFilePath, incrementFilePath);
	}

	/**
	 * TODO 导出源码路径下的文件，特殊处理java文件，转换成class文件
	 * oujie 2015-4-20 下午6:42:51
	 * @param filePath
	 */
	public void exportSrcFile(String filePath) {
		// 如果是src包下的则执行别的方式
		filePath = filePath.replace(".java", ".class");
		String currentSrc = getCurrentSrc(filePath);
		String targetFilePath = new StringBuilder(targetDir)
				.append(File.separator).append("context")
				.append(File.separator).append(srcDir).append(File.separator)
				.append(filePath.substring(currentSrc.length() + 1)).toString();
		String incrementFilePath = new StringBuilder(incrementDir)
				.append(File.separator)
				.append(srcDir)
				.append(File.separator)
				.append(filePath.substring(currentSrc.length() + 1)).toString();
		createParentFolder(incrementFilePath);
		copyFile(targetFilePath, incrementFilePath);
	}
	public void exportFile(String lineStr) {
		String filePath = lineStr;
		if (filePath.startsWith("context")) {
			exportWebFile(filePath);
		} else {
			exportSrcFile(filePath);
		}
	}

	private boolean checkFile(String filePath){
		File file = new File(targetDir,filePath);
		return file.exists() && file.isFile();
	}
	/**
	 * TODO 获取指定路径的根路径名称
	 * oujie 2015-4-20 下午6:43:45
	 * @param filePath
	 * @return
	 */
	private String getCurrentSrc(String filePath) {
		return filePath.split("/")[0];
	}

	/**
	 * 新建父节点目录
	 * @param filePath
	 *            目录
	 * @return 返回目录创建后的路径
	 */
	public void createParentFolder(String filePath) {
		try {
			File parentFile = new File(filePath).getParentFile();
			if (!parentFile.exists()) {
				parentFile.mkdirs();
			}
		} catch (Exception e) {
			log("创建目录操作出错", e, 0);
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
	public void copyFile(String oldFile, String newFile) {
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
			log(MessageFormat.format("COPY {0} TO {1} : SUCCESS", oldFile,newFile));
		} catch (Exception e) {
			log(MessageFormat.format("COPY {0} TO {1} : ERROR", oldFile,newFile), e, 0);
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
	private void closeIO(Closeable io) {
		if (io != null) {
			try {
				io.close();
			} catch (IOException e) {
				log("文件流关闭失败", e, 1);
			}
		}
	}

}
