package com.yhojo.demos.spring.jdbc.dao;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component("entityAccessRepository")
public class EntityAccessRepository {
	private Map<String, EntityAccess> entityAccessMap = new HashMap<>();

	public void registerEntityAccess(String entityName, EntityAccess entityAccess) {
		entityAccessMap.put(entityName, entityAccess);
	}

	public EntityAccess resolve(String entityName) {
		return entityAccessMap.get(entityName);
	}
}
