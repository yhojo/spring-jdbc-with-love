package com.yhojo.demos.spring.jdbc.with.love;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.yhojo.demos.spring.jdbc.dao.EntityAccess;
import com.yhojo.demos.spring.jdbc.dao.EntityAccessRepository;

public abstract class RepositoryBase<E> {
	protected EntityAccess entityInfo = new EntityAccess(MyPerson.class);
	protected Class<E> entityType;
	protected EntityAccessRepository entityAccessRepository;

	protected RepositoryBase(Class<E> entityType) {
		this.entityType = entityType;
		this.entityInfo = new EntityAccess(entityType);
	}

	@Autowired
	public void setEntityAccessRepository(EntityAccessRepository entityAccessRepository) {
		this.entityAccessRepository = entityAccessRepository;
		entityInfo.setEntityAccessRepository(entityAccessRepository);
	}

	@Autowired
	public void setDataSource(DataSource dataSource) {
		entityInfo.setDataSource(dataSource);
	}

	@Transactional
	public int insert(E entity) {
		return entityInfo.doInsert(entity);
	}

	@Transactional
	public int update(E entity) {
		return entityInfo.doUpdate(entity);
	}

	@Transactional
	protected E findByPkey(Object ... pkeys) {
		return entityInfo.findByPkey(entityType, pkeys);
	}

	@Transactional
	public int delete(MyPerson entity) {
		return entityInfo.doDelete(entity);
	}
}
