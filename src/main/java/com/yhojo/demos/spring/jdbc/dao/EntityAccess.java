package com.yhojo.demos.spring.jdbc.dao;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

public class EntityAccess {
	private Class<?> targetClass;
	private String name;
	private String tableName;
	private List<String> allFieldNames = new ArrayList<>();
	private List<String> pkeyFieldNames = new ArrayList<>();
	private List<String> bodyFieldNames = new ArrayList<>();
	private Map<String, FieldAccess> fieldAccessMap = new HashMap<>();
	private JdbcTemplate jdbc = new JdbcTemplate();
	private EntityAccessRepository entityAccessRepository;

	public EntityAccess(Class<?> clazz) {
		try {
			targetClass = clazz;
			// テーブル名を設定する
			EntityMap entityMap = clazz.getAnnotation(EntityMap.class);
			if (entityMap != null && !StringUtils.isEmpty(entityMap.tableName())) {
				tableName = entityMap.tableName();
			} else {
				tableName = clazz.getSimpleName();
			}
			if (entityMap != null && !StringUtils.isEmpty(entityMap.name())) {
				name = entityMap.name();
			} else {
				name = targetClass.getSimpleName();
			}
			// オブジェクトのフィールドからカラムへのマッピングを設定する
			for (final Field field: clazz.getFields()) {
				registerFieldAccess(FieldAccess.buildFromField(field));
			}
			// BeanPropertyからカラムへのマッピングを設定する
			BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
			for (final PropertyDescriptor desc: beanInfo.getPropertyDescriptors()) {
				registerFieldAccess(FieldAccess.buildFromBeanProperty(desc));
			}
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		}
	}

	public void setEntityAccessRepository(EntityAccessRepository entityAccessRepository) {
		this.entityAccessRepository = entityAccessRepository;
		entityAccessRepository.registerEntityAccess(name, this);
	}

	public void setDataSource(DataSource dataSource) {
		jdbc.setDataSource(dataSource);
	}

	private void registerFieldAccess(FieldAccess fieldAccess) {
		if (fieldAccess != null) {
			this.fieldAccessMap.put(fieldAccess.getName(), fieldAccess);
			this.allFieldNames.add(fieldAccess.getName());
			if (fieldAccess.isPkey()) {
				pkeyFieldNames.add(fieldAccess.getName());
			} else {
				bodyFieldNames.add(fieldAccess.getName());
			}
		}
	}

	public boolean hasPropertyNamed(String name) {
		return fieldAccessMap.containsKey(name);
	}

	public void setValue(Object entity, String name, Object value) {
		FieldAccess fieldAccess = fieldAccessMap.get(name);
		if (fieldAccess == null) {
			throw new RuntimeException("No such column " + name + " on " + entity);
		}
		fieldAccess.setValue(entity, value);
	}

	private String calcColNamesOf(String prefix, List<String> names) {
		if (prefix == null) {
			prefix = "";
		}
		List<String> colNames = new ArrayList<>(names.size());
		for (String name: names) {
			FieldAccess fieldAccess = fieldAccessMap.get(name);
			colNames.add(prefix + fieldAccess.getColName());
		}
		return StringUtils.join(colNames, ", ");
	}

	public String calcColNames(String prefix) {
		return calcColNamesOf(prefix, allFieldNames);
	}
	
	public String calcPkeyColNames() {
		return calcColNamesOf("", pkeyFieldNames);
	}

	public String calcBodyColNames() {
		return calcColNamesOf("", bodyFieldNames);
	}

	public List<FieldAccess> calcFieldAccessOf(List<String> names) {
		List<FieldAccess> fields = new ArrayList<>();
		for (String name: names) {
			fields.add(fieldAccessMap.get(name));
		}
		return fields;
	}

	static final Pattern ESCAPE_PATTERN = Pattern.compile("@\\{([^\\}]*)\\}");
	public String parseQuery(String src) {
		Matcher m = ESCAPE_PATTERN.matcher(src);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			List<String> expResults = new ArrayList<>();
			for (String exp: m.group(1).split(",")) {
				if (!StringUtils.isEmpty(exp)) {
					expResults.add(calcColumnName(exp));
				}
			}
			m.appendReplacement(sb, StringUtils.join(expResults, ", "));
		}
		m.appendTail(sb);
		return sb.toString();
	}

	protected String calcColumnName(String exp) {
		exp = exp.trim();
		if (StringUtils.equals("*", exp)) {
			return calcColNames(tableName + ".");
		} else if (StringUtils.equals("#TABLE_NAME", exp)) {
			return tableName;
		} else if (0 <= exp.indexOf('.')) {
			// 他のエンティティ参照式
			String[] split = exp.split("\\.");
			if (split.length != 2) {
				throw new RuntimeException("Entity reference is invalid: " + exp);
			}
			EntityAccess refAccess = entityAccessRepository.resolve(split[0]);
			if (refAccess == null) {
				throw new RuntimeException("Entity not found named " + split[0]);
			}
			return refAccess.calcColumnName(split[1]);
		} else {
			FieldAccess fieldAccess = fieldAccessMap.get(exp);
			if (fieldAccess == null) {
				throw new RuntimeException("field not found " + exp + " on " + targetClass.getName());
			}
			return tableName + "." + fieldAccess.getColName();
		}
	}

	public String calcWhereSql() {
		List<FieldAccess> fields = calcFieldAccessOf(pkeyFieldNames);
		List<String> colNames = new ArrayList<>(fields.size());
		for (FieldAccess fieldAccess: fields) {
			colNames.add(fieldAccess.getColName() + "=?");
		}
		return StringUtils.join(colNames, " AND ");
	}

	public String calcInsertSql() {
		String sql = "INSERT INTO " + tableName
				+ " (" + StringUtils.join(calcColNames("")) + ")"
				+ " VALUES (" + StringUtils.repeat("?", ", ", allFieldNames.size()) + ")";
		return sql;
	}

	public String calcSetSql() {
		List<FieldAccess> fields = calcFieldAccessOf(bodyFieldNames);
		List<String> colNames = new ArrayList<>(fields.size());
		for (FieldAccess fieldAccess: fields) {
			colNames.add(fieldAccess.getColName() + "=?");
		}
		return StringUtils.join(colNames, ", ");
	}

	public String calcUpdateSql() {
		String sql = "UPDATE " + tableName
				+ " SET " + calcSetSql()
				+ " WHERE " + calcWhereSql();
		return sql;
	}

	public String calcDeleteSql() {
		String sql = "DELETE FROM " + tableName
				+ " WHERE " + calcWhereSql();
		return sql;
	}

	public int doInsert(final Object entity) {
		return jdbc.update(calcInsertSql(), new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				int index = 1;
				for (FieldAccess fieldAccess: calcFieldAccessOf(allFieldNames)) {
					ps.setObject(index, fieldAccess.getValue(entity));
					index++;
				}
			}
		});
	}

	public int doUpdate(final Object entity) {
		return jdbc.update(calcUpdateSql(), new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				int index = 1;
				for (FieldAccess fieldAccess: calcFieldAccessOf(bodyFieldNames)) {
					ps.setObject(index, fieldAccess.getValue(entity), fieldAccess.getSqlType());
					index++;
				}
				for (FieldAccess fieldAccess: calcFieldAccessOf(pkeyFieldNames)) {
					ps.setObject(index, fieldAccess.getValue(entity), fieldAccess.getSqlType());
					index++;
				}
			}
		});
	}

	public int doDelete(final Object entity) {
		return jdbc.update(calcDeleteSql(), new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				int index = 1;
				for (FieldAccess fieldAccess: calcFieldAccessOf(pkeyFieldNames)) {
					Object value = fieldAccess.getValue(entity);
					ps.setObject(index, value, fieldAccess.getSqlType());
					index++;
				}
			}
		});
	}

	public <T> T findByPkey(final Class<T> clazz, Object ... pkeys) {
		return findOnly(clazz, calcFindByPkeySQL(), pkeys);
	}

	private String calcFindByPkeySQL() {
		String sql = "SELECT " + calcColNames("")
				+ " FROM " + tableName
				+ " WHERE " + calcWhereSql();
		return sql;
	}

	public <T> T findOnly(final Class<T> clazz, String sql, Object ...args) {
		return jdbc.query(sql,  new ResultSetExtractor<T>() {
			@Override
			public T extractData(ResultSet rs) throws SQLException,
					DataAccessException {
				if (rs.next()) {
					try {
						T entity = clazz.newInstance();
						int index = 1;
						for (String name: allFieldNames) {
							FieldAccess fieldAccess = fieldAccessMap.get(name);
							fieldAccess.setValue(entity, rs.getObject(index));
							index++;
						}
						return entity;
					} catch (InstantiationException | IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				} else {
					// 見つからなかった。
					return null;
				}
			}			
		}, args);
	}

	public <T> RowMapper<T> createRowMapper(final Class<T> entityClass) {
		return new RowMapper<T>() {
			@Override
			public T mapRow(ResultSet rs, int rowNum) throws SQLException {
				T entity;
				try {
					entity = entityClass.newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
				int index = 1;
				for (String name: allFieldNames) {
					FieldAccess fieldAccess = fieldAccessMap.get(name);
					fieldAccess.setValue(entity, rs.getObject(index));
					index++;
				}
				return entity;
			}
		};
	}
	
	public RowMapper<List<?>> joinRowMapper(final RowMapper<?> ... rowMappers) {
		RowMapper<List<?>> rowMapper = new RowMapper<List<?>>() {
			@Override
			public List<Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
				List<Object> list = new ArrayList<>();
				for (RowMapper<?> rowMapper: rowMappers) {
					list.add(rowMapper.mapRow(rs, rowNum));
					rowNum++;
				}
				return list;
			}
		};
		return rowMapper;
	}

	public <T> List<T> findListWithWRowMapper(final Class<T> entityClass, RowMapper<T> rowMapper, String sql, Object ...args) {
		return jdbc.query(sql, args, rowMapper);
	}

	public <T> List<T> findList(Class<T> entityClass, String sql, Object ...args) {
		return jdbc.query(sql, args, createRowMapper(entityClass));
	}
}
