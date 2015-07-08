package com.hztech.tools.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum DataSpaceTools {
	SPACE_3601("3601", 50005, 50057), SPACE_3602("3602", 50200, 50249), SPACE_3604(
			"3604", 50101, 50161), SPACE_3605("3605", 50300, 50315), SPACE_3606(
			"3606", 50600, 50619), SPACE_3611("3611", 51100, 51185);
	private final static String START_LIKE_SPACE_ALERT_FIELD = "SELECT X.OWNER FROM ALL_TABLES X WHERE NOT EXISTS( SELECT T.OWNER FROM ALL_TAB_COLUMNS T WHERE  T.TABLE_NAME = X.TABLE_NAME AND T.OWNER = X.OWNER AND T.COLUMN_NAME = '#ALERT_FIELD_NAME' ) AND X.OWNER  LIKE '#START_SPACE_LIKE%' AND X.TABLE_NAME = '#ALERT_TABLE_NAME'";
	private final static String UNION = "\n UNION \n";
	private final static String CREATE_VIEW = "CREATE OR REPLACE VIEW #VIEW_NAME AS \n ";
	private final static String DROP_VIEW = "DROP VIEW #VIEW_NAME ; \n";
	private final static String VIEW_NAME_PLACE_HOLDER = "#VIEW_NAME";
	private final static String START_SPACE_LIKE_PLACE_HOLDER = "#START_SPACE_LIKE";
	private final static String ALERT_FIELD_NAME_PLACE_HOLDER = "#ALERT_FIELD_NAME";
	private final static String ALERT_TABLE_NAME_PLACE_HOLDER = "#ALERT_TABLE_NAME";
	public final static String TABLE_SPACE_PLACE_HOLDER = "#NUM";
	public final static String SPACE_NAME_PLACE_HOLDER = "#SPACE";

	private String spaceName;
	private int beginSpace;
	private int endSpace;
	private Set<Integer> spaceSet;

	private DataSpaceTools(String spaceName, final int beginSpace,
			final int endSpace) {
		checkEmpty(spaceName);
		if (endSpace < beginSpace) {
			throw new RuntimeException("endSpace can't less than beginSpace!");
		}
		this.spaceName = spaceName;
		this.beginSpace = beginSpace;
		this.endSpace = endSpace;
		spaceSet = new HashSet<Integer>() {
			private static final long serialVersionUID = 6728088115548365426L;
			{
				for (int spaceNum = beginSpace; spaceNum <= endSpace; spaceNum++) {
					add(spaceNum);
				}
			}
		};
	}

	private static void checkEmpty(String str) {
		if (str == null || "".equals(str.trim()))
			throw new NullPointerException("paramter can't be null!");
	}

	private String buildNumSqlByUnion(String sql, List<Integer> tableSpaceNums) {
		checkEmpty(sql);
		StringBuilder buildSQL = new StringBuilder();
		String targetSQL = sql.replaceAll(SPACE_NAME_PLACE_HOLDER, spaceName);
		Collections.sort(tableSpaceNums);// ≈≈–Ú
		for (Integer tableSpaceNum : tableSpaceNums) {
			buildSQL.append(
					targetSQL.replaceAll(TABLE_SPACE_PLACE_HOLDER,
							String.valueOf(tableSpaceNum))).append(UNION);
		}
		buildSQL.delete(buildSQL.lastIndexOf(UNION), buildSQL.length());
		return buildSQL.append(";").toString();
	}

	public String buildContainsNumSqlByUnion(String sql,
			List<Integer> containNums) {
		Set<Integer> intersectionSpaceNums = new HashSet<Integer>();
		intersectionSpaceNums.clear();
		intersectionSpaceNums.addAll(spaceSet);
		intersectionSpaceNums.retainAll(containNums);
		return buildNumSqlByUnion(sql, new ArrayList<Integer>(
				intersectionSpaceNums));
	}

	public String buildRemoveNumSqlByUnion(String sql, List<Integer> removeNums) {
		Set<Integer> differenceSetSpaceNums = new HashSet<Integer>();
		differenceSetSpaceNums.clear();
		differenceSetSpaceNums.addAll(spaceSet);
		differenceSetSpaceNums.retainAll(removeNums);
		return buildNumSqlByUnion(sql, new ArrayList<Integer>(
				differenceSetSpaceNums));
	}

	public String buildSqlByUnion(String sql) {
		return buildNumSqlByUnion(sql, new ArrayList<Integer>(spaceSet));
	}

	public String buidCreatViewByUnion(String viewName, String sql) {
		checkEmpty(viewName);
		StringBuilder createViewSql = new StringBuilder();
		createViewSql.append(
				CREATE_VIEW.replace(VIEW_NAME_PLACE_HOLDER,
						viewName.replace(SPACE_NAME_PLACE_HOLDER, spaceName)))
				.append(buildSqlByUnion(sql));
		return createViewSql.toString();
	}

	public String buildDropedCreatViewByUnion(String viewName, String sql) {
		checkEmpty(viewName);
		StringBuilder dropedCreateViewSql = new StringBuilder();
		dropedCreateViewSql.append(
				DROP_VIEW.replace(VIEW_NAME_PLACE_HOLDER,
						viewName.replace(SPACE_NAME_PLACE_HOLDER, spaceName)))
				.append(buidCreatViewByUnion(viewName, sql));
		return dropedCreateViewSql.toString();
	}

	public String getSpaceName() {
		return spaceName;
	}

	public int getBeginSpace() {
		return beginSpace;
	}

	public int getEndSpace() {
		return endSpace;
	}

	public static String buildAlertFieldStartSpace(String startSpace,
			String tableName, String field) {
		return buildAlertFieldStartSpace(startSpace, tableName, field, "VARCHAR2(50)");
	}
	public static String buildAlertFieldStartSpace(String startSpace,
			String tableName, String field, String fieldType) {
		checkEmpty(tableName);
		checkEmpty(field);
		StringBuilder alertFieldSQL = new StringBuilder();
		alertFieldSQL.append(" begin \n for y in (");
		alertFieldSQL.append(START_LIKE_SPACE_ALERT_FIELD
				.replace(START_SPACE_LIKE_PLACE_HOLDER, startSpace)
				.replace(ALERT_TABLE_NAME_PLACE_HOLDER, tableName)
				.replace(ALERT_FIELD_NAME_PLACE_HOLDER, field));
		alertFieldSQL.append(") Loop execute immediate 'alter table ' || y.owner ||'.");
		alertFieldSQL.append(tableName);
		alertFieldSQL.append(" ADD ");
		alertFieldSQL.append(field).append(" ");
		alertFieldSQL.append(fieldType);
		alertFieldSQL.append("'; end loop; \n end;\n /\n ;");
		return alertFieldSQL.toString();
	}
	
	public static void main(String[] args) {
		System.out.println(buildAlertFieldStartSpace("HYTENP_", "TSSTUAPPLICATION", "APPREMARKDOC"));
	}
}
