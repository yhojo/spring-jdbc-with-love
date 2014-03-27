package com.yhojo.demos.spring.jdbc.with.love;

import com.yhojo.demos.spring.jdbc.dao.ColMap;
import com.yhojo.demos.spring.jdbc.dao.EntityMap;
import com.yhojo.demos.spring.jdbc.dao.PkeyMap;

@EntityMap(tableName = "PERSON")
public class MyPerson {
	@ColMap(colName = "PERSON_ID")
	@PkeyMap
	public long id;

	@ColMap
	public String name;

	@ColMap
	public int age;

	private String role;

	@ColMap
	public void setRole(String role) {
		this.role = role;
	}

	public String getRole() {
		return role;
	}
	
	private long groupId;

	@ColMap(colName = "GROUP_ID")
	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}

	public long getGroupId() {
		return groupId;
	}
}
