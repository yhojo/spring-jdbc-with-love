package com.yhojo.demos.spring.jdbc.with.love;

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
}
