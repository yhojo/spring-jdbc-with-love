package com.yhojo.demos.spring.jdbc.with.love;

import org.springframework.stereotype.Repository;

@Repository("groupRepository")
public class MyGroupRepository extends RepositoryBase<MyGroup> {

	public MyGroupRepository() {
		super(MyGroup.class);
	}

}
