package com.yhojo.demos.spring.jdbc.with.love;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("sampleRepository")
public class MySampleRepository extends RepositoryBase<MyPerson> {
	public MySampleRepository() {
		super(MyPerson.class);
	}

	@Transactional
	public MyPerson findById(long id) {
		return findByPkey(id);
	}

	@Transactional
	public List<MyPerson> findAll() {
		String sql = entityInfo.parseQuery("SELECT @{*} FROM @{#TABLE_NAME} ORDER BY @{id}");
		return entityInfo.findList(MyPerson.class, sql);
	}
}
