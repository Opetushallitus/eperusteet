package fi.vm.sade.eperusteet.hibernate;

import org.hibernate.dialect.PostgreSQL9Dialect;
import org.hibernate.dialect.function.StandardSQLFunction;

public class CustomPostgreSQL9Dialect extends PostgreSQL9Dialect {
    public CustomPostgreSQL9Dialect() {
        super();
        registerFunction("textsearch", new PostgreTextSearchFunction());
        StandardSQLFunction rakenna_haku = new StandardSQLFunction("rakenna_haku");
        registerFunction("rakenna_haku", rakenna_haku);
    }
}
