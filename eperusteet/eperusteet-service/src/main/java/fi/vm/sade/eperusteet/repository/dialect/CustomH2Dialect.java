package fi.vm.sade.eperusteet.repository.dialect;

import org.hibernate.dialect.H2Dialect;

import java.sql.Types;

public class CustomH2Dialect extends H2Dialect {
    public CustomH2Dialect() {
        super();
        registerColumnType(Types.JAVA_OBJECT, "jsonb");
    }
}
