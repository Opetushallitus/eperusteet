package fi.vm.sade.eperusteet.repository.dialect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fi.vm.sade.eperusteet.config.InitJacksonConverter;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.IOException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

public class JsonBType implements UserType, Serializable {
    private final ObjectMapper mapper = InitJacksonConverter.createMapper();

    @Override
    public int getSqlType() {
        return Types.JAVA_OBJECT;
    }

    @Override
    public Class<ObjectNode> returnedClass() {
        return ObjectNode.class;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return Objects.equals(x, y);
    }

//    @Override
//    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws HibernateException, SQLException {
//        String str = rs.getString(names[0]);
//        if (str != null) {
//            try {
//                JsonNode node = mapper.readTree(str);
//                return node;
//            } catch (IOException e) {
//                throw new BusinessRuleViolationException("datan-luku-epaonnistui");
//            }
//        }
//        return JsonNodeFactory.instance.objectNode();
//    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        if (value != null) {
//            String jsonStr = gson.toJson(value);
            try {
                String s = mapper.writeValueAsString(value);
                st.setObject(index, s, Types.OTHER);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                throw new BusinessRuleViolationException("kirjoitus-epaonnistui");
            }
        }
        else {
            st.setNull(index, Types.NULL);
        }
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        try {
            byte[] bytes = mapper.writeValueAsBytes(value);
            JsonNode node = mapper.readTree(bytes);
            return node;
        } catch (IOException e) {
            e.printStackTrace();
            throw new BusinessRuleViolationException("copy-failed");
        }
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return 0;
    }

    @Override
    public Object nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException {
        return null;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable)value;
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return deepCopy(cached);
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return deepCopy(original);
    }
}
