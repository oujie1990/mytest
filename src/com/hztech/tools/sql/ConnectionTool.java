package com.hztech.tools.sql;

import static com.hztech.tools.basic.FileUtils.closeIO;
import static com.hztech.tools.basic.FileUtils.existFile;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class ConnectionTool {
	private static final String urlKey = "jdbc.url";
	private static final String userKey = "jdbc.user";
	private static final String pwdKey = "jdbc.pwd";
	private String url;
	private String user;
	private String pwd;
	private Connection con;

	static{
		try {
			Class.forName("oracle.jdbc.OracleDriver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("加载数据库驱动类失败！");
		}
	}
	
	private ConnectionTool(String url, String user, String pwd) {
		this.url = url;
		this.user = user;
		this.pwd = pwd;
		try {
			buildConnection();
		} catch (SQLException e) {
			throw new RuntimeException("初始化数据库工具类失败!");
		}
	}

	public String getUrl() {
		return url;
	}

	public String getUser() {
		return user;
	}

	public String getPwd() {
		return pwd;
	}
	
	private void buildConnection() throws SQLException{
		con = DriverManager.getConnection(url, user, pwd);
	}
	
	public void query(String sql, CallBack callBack){
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(sql);
			callBack.doCallBack(rs);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally{
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {}
			}
		}
	}
	
	/**
	 * TODO oujie 2015-6-12 下午3:05:45
	 * 
	 * @param propPath
	 * @return
	 */
	public static ConnectionTool getTool(String propPath) {
		if (existFile(propPath)) {
			throw new NullPointerException("propPath isn't a file!");
		}

		Properties props = new Properties();
		Reader reader = null;
		try {
			reader = new FileReader(new File(propPath));
			props.load(reader);
		} catch (Exception e) {
			throw new RuntimeException("加载配置文件失败");
		} finally {
			closeIO(reader);
		}

		return new ConnectionTool(props.getProperty(urlKey),
				props.getProperty(userKey), props.getProperty(pwdKey));
	}
	
	public static interface CallBack{
		void doCallBack(ResultSet rs);
	}
}
