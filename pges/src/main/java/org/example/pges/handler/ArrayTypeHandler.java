package org.example.pges.handler;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * @author: Admin
 * @date: 2021/12/24
 * @description:
 */
public class ArrayTypeHandler extends BaseTypeHandler<Object[]> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArrayTypeHandler.class);

    private static final String TYPE_NAME_VARCHAR = "varchar";
    private static final String TYPE_NAME_INTEGER = "integer";

    private static final String TYPE_NAME_INTEGER_8 = "int8";
    private static final String TYPE_NAME_BOOLEAN = "boolean";
    private static final String TYPE_NAME_NUMERIC = "numeric";

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object[] parameter, JdbcType jdbcType)
            throws SQLException {
        String typename = null;
        if (parameter instanceof Integer[]) {
            typename = TYPE_NAME_INTEGER;
        } else if (parameter instanceof String[]) {
            typename = TYPE_NAME_VARCHAR;
        } else if (parameter instanceof Boolean[]) {
            typename = TYPE_NAME_BOOLEAN;
        } else if (parameter instanceof Double[]) {
            typename = TYPE_NAME_NUMERIC;
        }else if(parameter instanceof Long[]){
            typename = TYPE_NAME_INTEGER_8;
        }

        if (typename == null) {
            throw new TypeException("arraytypehandler parameter typename error, your type is " + parameter.getClass().getName());
        }

        // 这2行是关键的代码，创建array，然后ps.setarray(i, array)就可以了
        Array array = ps.getConnection().createArrayOf(typename, parameter);
        ps.setArray(i, array);
    }

    @Override
    public Object[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return getArray(rs.getArray(columnName));
    }


    @Override
    public Object[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return getArray(rs.getArray(columnIndex));
    }


    @Override
    public Object[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return getArray(cs.getArray(columnIndex));
    }

    private Long[] getArray(Array array) throws PSQLException {

        if (array == null) {
            return null;
        }
        try {
            Long[] longObjects = (Long[]) array.getArray();
            return longObjects;
        } catch (SQLException e) {
            throw new PSQLException("Unable to convert int8[] to long[]", PSQLState.DATA_TYPE_MISMATCH, e);
        }
    }
}