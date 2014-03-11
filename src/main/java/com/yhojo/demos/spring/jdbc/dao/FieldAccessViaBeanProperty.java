package com.yhojo.demos.spring.jdbc.dao;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FieldAccessViaBeanProperty extends FieldAccess {
	private PropertyDescriptor desc;

	public FieldAccessViaBeanProperty(PropertyDescriptor desc) {
		super(desc.getName());
		this.desc = desc;
	}

	protected <T extends Annotation> T getAnnotation(Class<T> annotationType) {
		Method method = desc.getWriteMethod();
		if (method != null && method.getAnnotation(ColMap.class) != null) {
			return method.getAnnotation(annotationType);
		}
		method = desc.getReadMethod();
		if (method != null) {
			return method.getAnnotation(annotationType);
		}
		return null;
	}

	@Override
	public Class<?> getFieldType() {
		return desc.getPropertyType();
	}

	@Override
	public void setValue(Object entity, Object value) {
		try {
			desc.getWriteMethod().invoke(entity, value);
		} catch (IllegalAccessException
				| IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	@Override
	public Object getValue(Object entity) {
		try {
			return desc.getReadMethod().invoke(entity);
		} catch (IllegalAccessException
				| IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}
