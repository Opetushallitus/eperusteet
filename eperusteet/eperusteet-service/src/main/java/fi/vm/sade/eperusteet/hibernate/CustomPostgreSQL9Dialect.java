package fi.vm.sade.eperusteet.hibernate;

import org.hibernate.dialect.PostgreSQL9Dialect;

public class CustomPostgreSQL9Dialect extends PostgreSQL9Dialect {
    public CustomPostgreSQL9Dialect() {
        super();
        registerFunction("textsearch", new PostgreTextSearchFunction());
    }
}
