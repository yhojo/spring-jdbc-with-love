package com.yhojo.demos.spring.jdbc.dao;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Types;

import org.apache.commons.lang3.StringUtils;

public abstract class FieldAccess {
	private String name;
	private ColMap colMap;
	private PkeyMap pkeyMap;
	private String colName;

	public static FieldAccess buildFromBeanProperty(PropertyDescriptor pd) {
		return nullOrValidFieldAccess(new FieldAccessViaBeanProperty(pd));
	}

	public static FieldAccess buildFromField(Field field) {
		return nullOrValidFieldAccess(new FieldAccessViaField(field));
	}

	private static FieldAccess nullOrValidFieldAccess(FieldAccess fieldAccess) {
		fieldAccess.initAnnotations();
		if (fieldAccess.getColMap() != null) {
			return fieldAccess;
		} else {
			return null;
		}
	}

	public FieldAccess(String name) {
		this.name = name;
	}

	protected void initAnnotations() {
		colMap = getAnnotation(ColMap.class);
		if (colMap == null || StringUtils.isEmpty(colMap.colName())) {
			colName = name;
		} else {
			colName = colMap.colName();
		}
		pkeyMap = getAnnotation(PkeyMap.class);
	}

	protected abstract <T extends Annotation> T getAnnotation(Class<T> annotationType); 

	public abstract void setValue(Object entity, Object value);

	public abstract Object getValue(Object entity);

	public abstract Class<?> getFieldType();

	public int getSqlType() {
		Class<?> fieldType = getFieldType();
		if (String.class.isAssignableFrom(fieldType)) {
			return Types.VARCHAR;
		} else if (Long.TYPE.isAssignableFrom(fieldType)) {
			return Types.NUMERIC;
		} else if (java.sql.Date.class.isAssignableFrom(fieldType)) {
			return Types.DATE;
		} else if (java.util.Date.class.isAssignableFrom(fieldType)) {
			return Types.TIMESTAMP;
		} else if (BigDecimal.class.isAssignableFrom(fieldType)) {
			return Types.DECIMAL;
		} else if (Integer.TYPE.isAssignableFrom(fieldType)) {
			return Types.NUMERIC;
		} else {
			throw new RuntimeException("Unknown SQL Type mapping for: " + fieldType);
		}
	}

	public boolean isPkey() {
		return pkeyMap != null;
	}

	public String getName() {
		return name;
	}

	public String getColName() {
		return colName;
	}

	public ColMap getColMap() {
		return colMap;
	}
}
