package com.sos.hibernate.classes;

import java.util.Collection;
import java.util.stream.Collectors;

public class SearchStringHelper {

	public static String getSearchOperator(String s) {
		if (s.contains("%")) {
			return " like ";
		} else {
			return "=";
		}
	}

	public static String getSearchPathOperator(String s) {
		s = getSearchPathValue(s);
		if (s.contains("%")) {
			return "like";
		} else {
			return "=";
		}
	}

	public static String getSearchPathValue(String s) {
		if (s.startsWith("/") || s.startsWith("./") || s.startsWith("%")) {
			return s;
		} else {
			return "%" + s;
		}
	}

	public static String getStringSetSql(Collection<String> values, String fieldName) {
		StringBuilder sql = new StringBuilder();

		for (String s : values) {
			sql.append(fieldName + getSearchOperator(s) + "'" + s + "'").append(" or ");
		}
		sql.append("1=0");

		return " (" + sql.toString() + ") ";
	}

	public static String getIntegerSetSql(Collection<Integer> values, String fieldName) {
		StringBuilder sql = new StringBuilder();

		for (Integer i : values) {
			String s = String.valueOf(i);
			sql.append(fieldName + "=" + s).append(" or ");
		}
		sql.append("1=0");

		return " (" + sql.toString() + ") ";
	}

	public static String getSetPathSql(Collection<String> values, String fieldName) {
		StringBuilder sql = new StringBuilder();

		for (String s : values) {
			s = getSearchPathValue(s);
			sql.append(fieldName + getSearchOperator(s) + s).append(" or ");
		}
		sql.append("1=0");

		return " (" + sql.toString() + ") ";
	}

	public static String getStringListSql(Collection<String> values, String fieldName) {
		StringBuilder sql = new StringBuilder();

		for (String s : values) {
			sql.append(fieldName + getSearchOperator(s) + "'" + s + "'").append(" or ");
		}
		
	     String s = sql.toString();
	     s = s.substring(0, s.length() - 4);
	     return " (" + s + ") ";
	}

    public static String getStringListPathSql(Collection<String> values, String fieldName) {
        return values.stream().map(s -> getSearchPathValue(s)).map(s -> fieldName + getSearchOperator(s) + "'" + s + "'").collect(Collectors.joining(
                " or ", " (", ") "));
	}

	public static boolean isDBWildcardSearch(String regex) {
		return (regex != null && (regex.contains("%") || regex.contains(",")));
	}

	public static String getRegexValue(String sourceFilesRegex) {
		if (sourceFilesRegex != null) {
			return sourceFilesRegex.replaceAll("%", ".*");
		}
		return null;
	}
}
