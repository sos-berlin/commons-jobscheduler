package com.sos.hibernate.classes;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class TestSearchStringHelper {

	@Test
	public void testGetSearchPathOperator() {
		String s2 = SearchStringHelper.getSearchPathOperator("test%test");
		assertEquals("testGetSearchPathOperator", s2, "like");
		s2 = SearchStringHelper.getSearchPathOperator("/test test");
		assertEquals("testGetSearchPathOperator", s2, "=");
		s2 = SearchStringHelper.getSearchPathOperator("test test");
		assertEquals("testGetSearchPathOperator", s2, "like");
	}

	@Test
	public void testGetSearchOperator() {
		String s2 = SearchStringHelper.getSearchOperator("test%test");
		assertEquals("testGetSearchOperator", " like ", s2);
		s2 = SearchStringHelper.getSearchOperator("/test test");
		assertEquals("testGetSearchOperator", "=", s2);
		s2 = SearchStringHelper.getSearchOperator("test test");
		assertEquals("testGetSearchOperator", "=", s2);
	}

	@Test
	public void testGetSearchPathValue() {
		String s2 = SearchStringHelper.getSearchPathValue("testtest");
		assertEquals("testGetSearchPathValue", "%testtest", s2);
		s2 = SearchStringHelper.getSearchPathValue("/testtest");
		assertEquals("testGetSearchOperator", "/testtest", s2);
	}

	@Test
	public void testGetStringSetSql() {
		Set<String> values = new HashSet<String>();
		values.add("test");
		String s2 = SearchStringHelper.getStringSetSql(values, "fieldname");
		System.out.println(s2);
		assertEquals("testGetSetSql", "(fieldname='test' or 1=0)", s2);

		values = new HashSet<String>();
		values.add("te%st");
		s2 = SearchStringHelper.getStringSetSql(values, "fieldname");
		System.out.println(s2);
		assertEquals("testGetSetSql", "(fieldname like 'te%st' or 1=0)", s2);

		values = new HashSet<String>();
		values.add("te%st");
		values.add("test");
		values.add("%test");
		s2 = SearchStringHelper.getStringSetSql(values, "fieldname");
		System.out.println(s2);
		assertEquals("testGetStringSetSql",
				"(fieldname like 'te%st' or fieldname='test' or fieldname like '%test' or 1=0)", s2);
 
	}

	@Test
	public void testGetIntegerSetSql() {
		Set<Integer> values = new HashSet<Integer>();
		values.add(1);
		String s2 = SearchStringHelper.getIntegerSetSql(values, "fieldname");
		System.out.println(s2);
		assertEquals("testGetSetSql", "(fieldname=1 or 1=0)", s2);

		values = new HashSet<Integer>();
		values.add(2);
		s2 = SearchStringHelper.getIntegerSetSql(values, "fieldname");
		System.out.println(s2);
		assertEquals("testGetSetSql", "(fieldname=2 or 1=0)", s2);

		values = new HashSet<Integer>();
		values.add(1);
		values.add(2);
		values.add(3);
		s2 = SearchStringHelper.getIntegerSetSql(values, "fieldname");
		System.out.println(s2);
		assertEquals("testGetStringSetSql", "(fieldname=1 or fieldname=2 or fieldname=3 or 1=0)", s2);
	}

	@Test
	public void testGetSetPathSql() {
		Set<String> values = new HashSet<String>();
		values.add("test");
		String s2 = SearchStringHelper.getSetPathSql(values, "fieldname");
		System.out.println(s2);
		assertEquals("testGetSetPathSql", s2, "(fieldname like %test or 1=0)");

		values = new HashSet<String>();
		values.add("te%st");
		s2 = SearchStringHelper.getSetPathSql(values, "fieldname");
		System.out.println(s2);
		assertEquals("testGetSetPathSql", s2, "(fieldname like %te%st or 1=0)");

		values = new HashSet<String>();
		values.add("te%st");
		values.add("test");
		values.add("%test");
		s2 = SearchStringHelper.getSetPathSql(values, "fieldname");
		System.out.println(s2);
		assertEquals("testGetSetPathSql", s2,
				"(fieldname like %te%st or fieldname like %test or fieldname like %test or 1=0)");

		values = new HashSet<String>();
		values.add("/te%st");
		values.add("/rest");
		values.add("%fest");
		values.add("fest");
		s2 = SearchStringHelper.getSetPathSql(values, "fieldname");
		System.out.println(s2);
		assertEquals("testGetSetPathSql", s2,
				"(fieldname like /te%st or fieldname=/rest or fieldname like %fest or fieldname like %fest or 1=0)");

	}

	@Test
	public void testGetStringListSql() {
		// public static String getSetSql(Set<String> values, String fieldName) { }
		ArrayList<String> values = new ArrayList<String>();
		values.add("test");
		String s2 = SearchStringHelper.getStringListSql(values, "fieldname");
		System.out.println(s2);
		assertEquals("testGetSetPathSql", s2, "(fieldname='test' or 1=0)");

		values = new ArrayList<String>();
		values.add("te%st");
		s2 = SearchStringHelper.getStringListSql(values, "fieldname");
		System.out.println(s2);
		assertEquals("testGetSetPathSql", s2, "(fieldname like 'te%st' or 1=0)");

		values = new ArrayList<String>();
		values.add("te%st");
		values.add("test");
		values.add("%test");
		s2 = SearchStringHelper.getStringListSql(values, "fieldname");
		System.out.println(s2);
		assertEquals("testGetSetPathSql", s2,
				"(fieldname like 'te%st' or fieldname='test' or fieldname like '%test' or 1=0)");

		values = new ArrayList<String>();
		values.add("/te%st");
		values.add("/rest");
		values.add("%fest");
		values.add("fest");
		s2 = SearchStringHelper.getStringListSql(values, "fieldname");
		System.out.println(s2);
		assertEquals("testGetSetPathSql", s2,
				"(fieldname like '/te%st' or fieldname='/rest' or fieldname like '%fest' or fieldname='fest' or 1=0)");
	}
	
	@Test
	public void testGetStringListPathSql() {
		// public static String getSetSql(Set<String> values, String fieldName) { }
		ArrayList<String> values = new ArrayList<String>();
		values.add("test");
		String s2 = SearchStringHelper.getStringListPathSql(values, "fieldname");
		System.out.println(s2);
		assertEquals("testGetSetPathSql", s2, "(fieldname like '%test' or 1=0)");

		values = new ArrayList<String>();
		values.add("te%st");
		s2 = SearchStringHelper.getStringListPathSql(values, "fieldname");
		System.out.println(s2);
		assertEquals("testGetSetPathSql", s2, "(fieldname like '%te%st' or 1=0)");

		values = new ArrayList<String>();
		values.add("te%st");
		values.add("test");
		values.add("%test");
		s2 = SearchStringHelper.getStringListPathSql(values, "fieldname");
		System.out.println(s2);
		assertEquals("testGetSetPathSql", s2,
				"(fieldname like '%te%st' or fieldname like '%test' or fieldname like '%test' or 1=0)");

		values = new ArrayList<String>();
		values.add("/te%st");
		values.add("/rest");
		values.add("%fest");
		values.add("fest");
		s2 = SearchStringHelper.getStringListPathSql(values, "fieldname");
		System.out.println(s2);
		assertEquals("testGetSetPathSql", s2,
				"(fieldname like '/te%st' or fieldname='/rest' or fieldname like '%fest' or fieldname like '%fest' or 1=0)");

	}

}
