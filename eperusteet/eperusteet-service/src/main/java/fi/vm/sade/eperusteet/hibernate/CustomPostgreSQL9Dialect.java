package fi.vm.sade.eperusteet.hibernate;

import org.hibernate.dialect.PostgreSQL9Dialect;
import org.hibernate.dialect.function.StandardSQLFunction;

public class CustomPostgreSQL9Dialect extends PostgreSQL9Dialect {
    public CustomPostgreSQL9Dialect() {
        super();
        registerFunction("textsearch", new PostgreTextSearchFunction());
        registerFunction("rakenna_tekstihaku", new StandardSQLFunction("rakenna_haku"));
    }
}
