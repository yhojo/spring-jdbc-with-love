package com.yhojo.demos.spring.jdbc.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EntityAccessTest {
	EntityAccess entityAccess = new EntityAccess(DaoTestEntity.class);

	@Test
	public void testFieldAccess() {
		assertTrue(entityAccess.hasPropertyNamed("name"));
		assertTrue(entityAccess.hasPropertyNamed("age"));

		DaoTestEntity testEntity = new DaoTestEntity();
		assertThat(testEntity.name, is(nullValue()));
		assertThat(testEntity.age, is(0));

		entityAccess.setValue(testEntity, "name", "My Name");
		assertThat(testEntity.name, is("My Name"));
		
		entityAccess.setValue(testEntity, "age", 12);
		assertThat(testEntity.age, is(12));
	}

	@Test
	public void testRendarColoumnNames() {
		assertEquals("PERSON_ID, THE_NAME, age", entityAccess.calcColNames());
		assertEquals("SELECT PERSON_ID, THE_NAME, age", entityAccess.parseQuery("SELECT @{*}"));
		assertEquals("SELECT THE_NAME", entityAccess.parseQuery("SELECT @{name}"));
	}

	@Test
	public void testInsertSQL() {
		assertEquals("INSERT INTO PERSONS"
				+ " (PERSON_ID, THE_NAME, age)"
				+ " VALUES (?, ?, ?)", 
				entityAccess.calcInsertSql());
	}

	@Test
	public void testUpdateSQL() {
		assertEquals("UPDATE PERSONS"
				+ " SET THE_NAME=?, age=?"
				+ " WHERE PERSON_ID=?", entityAccess.calcUpdateSql());
	}

	@Test
	public void testDeleteSQL() {
		assertEquals("DELETE FROM PERSONS"
				+ " WHERE PERSON_ID=?", entityAccess.calcDeleteSql());
	}
}
