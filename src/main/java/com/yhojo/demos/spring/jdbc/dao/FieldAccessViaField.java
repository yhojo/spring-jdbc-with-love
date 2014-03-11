package com.yhojo.demos.spring.jdbc.dao;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class FieldAccessViaField extends FieldAccess {
	private Field field;

	public FieldAccessViaField(Field field) {
		super(field.getName());
		this.field = field;
	}

	@Override
	protected <T extends Annotation> T getAnnotation(Class<T> annotationType) {
		return field.getAnnotation(annotationType);
	}

	@Override
	public Class<?> getFieldType() {
		return field.getType();
	}

	@Override
	public void setValue(Object entity, Object value) {
		try {
			field.set(entity, value);
		} catch (IllegalArgumentException
				| IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	@Override
	public Object getValue(Object entity) {
		try {
			return field.get(entity);
		} catch (IllegalArgumentException
				| IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
