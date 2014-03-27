package com.yhojo.demos.spring.jdbc.with.love;

import com.yhojo.demos.spring.jdbc.dao.ColMap;
import com.yhojo.demos.spring.jdbc.dao.EntityMap;
import com.yhojo.demos.spring.jdbc.dao.PkeyMap;

@EntityMap(tableName = "PERSON_GROUP", name="Group")
public class MyGroup {
	@ColMap(colName="GROUP_ID")
	@PkeyMap
	public long id;

	@ColMap
	public String name;
}
