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
 * TODO ��� svnant �������ȡ��Ŀ������
 * @author oujie
 * 2015-4-20 ����6:34:51
 */
public class ExportIncrementFilesTask extends Task {
	// WEB-INF/classes
	private String srcDir = new StringBuilder("WEB-INF").append(File.separator)
			.append("classes").toString();
	// Ŀ���ļ�
	private String targetFile;
	// Ŀ���ļ�Ŀ¼
	private String targetDir;
	// ������Ŀ¼
	private String incrementDir;
	// �ų������Ŀ¼
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
	 * TODO ���ant���ò���
	 * oujie 2015-4-20 ����6:38:59
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
	 * TODO ��ȡsvn����ͳ�ƻ����ļ���
	 * ���ļ�������ָ��Ŀ¼�£�������Ŀ������
	 * oujie 2015-4-20 ����6:39:16
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
					.format("========================�ܹ��ύ���ļ�����{0}��,��Ч�����ļ���{1}��========================",
							count, vaildCount));
			raf.close();
		} catch (Exception e) {
			new BuildException(e);
		} finally {
			closeIO(raf);
		}
	}

	/**
	 * TODO �ų���Ŀ���ļ�����ʱΪ(.��־��ͷ���ļ����ļ���)
	 * oujie 2015-4-20 ����6:40:32
	 * oujie 2015-06-12 11:08:30 �޸� �ų�SVN�ļ��п���
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
			// ��(.)���ſ�ͷ���ļ�
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
	 * TODO �ַ����п�
	 * oujie 2015-4-20 ����6:41:24
	 * @param str
	 * @return
	 */
	private static boolean isBlank(String str) {
		return str == null || str.trim().equals("");
	}

	/**
	 * TODO ����Web���ļ������Ǵ����webapp�µ��ļ���������Դ��·�����ļ�
	 * oujie 2015-4-20 ����6:41:43
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
	 * TODO ����Դ��·���µ��ļ������⴦��java�ļ���ת����class�ļ�
	 * oujie 2015-4-20 ����6:42:51
	 * @param filePath
	 */
	public void exportSrcFile(String filePath) {
		// �����src���µ���ִ�б�ķ�ʽ
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
	 * TODO ��ȡָ��·���ĸ�·������
	 * oujie 2015-4-20 ����6:43:45
	 * @param filePath
	 * @return
	 */
	private String getCurrentSrc(String filePath) {
		return filePath.split("/")[0];
	}

	/**
	 * �½����ڵ�Ŀ¼
	 * @param filePath
	 *            Ŀ¼
	 * @return ����Ŀ¼�������·��
	 */
	public void createParentFolder(String filePath) {
		try {
			File parentFile = new File(filePath).getParentFile();
			if (!parentFile.exists()) {
				parentFile.mkdirs();
			}
		} catch (Exception e) {
			log("����Ŀ¼��������", e, 0);
		}
	}

	/**
	 * ���Ƶ����ļ�
	 * 
	 * @param oldFile
	 *            ׼�����Ƶ��ļ�Դ
	 * @param newFile
	 *            �������¾���·�����ļ���
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
	 * TODO �ر�������
	 * oujie 2015-4-20 ����6:44:46
	 * @param io
	 */
	private void closeIO(Closeable io) {
		if (io != null) {
			try {
				io.close();
			} catch (IOException e) {
				log("�ļ����ر�ʧ��", e, 1);
			}
		}
	}

}
