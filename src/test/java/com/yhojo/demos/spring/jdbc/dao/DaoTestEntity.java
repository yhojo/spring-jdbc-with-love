package com.yhojo.demos.spring.jdbc.dao;

@EntityMap(tableName = "PERSONS")
public class DaoTestEntity {
	@ColMap(colName = "PERSON_ID")
	@PkeyMap
	public String id;

	@ColMap(colName = "THE_NAME")
	public String name;

	@ColMap
	public int age;
}
