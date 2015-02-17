/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package db.migration;

import com.googlecode.flyway.core.api.migration.spring.SpringJdbcMigration;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

/**
 *
 * @author jhyoty
 */
public class V0_104__oppiaineen_tunniste implements SpringJdbcMigration {

    @Override
    public void migrate(final JdbcTemplate jdbcTemplate) throws Exception {
        for (String t : Arrays.asList("yl_oppiaine")) {
            fixUUID(jdbcTemplate, t);
        }
    }

    private void fixUUID(final JdbcTemplate jdbcTemplate, final String tableName) {

        jdbcTemplate.query("SELECT id FROM " + tableName, new RowCallbackHandler() {

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                Long id = rs.getLong(1);
                UUID uuid = UUID.randomUUID();
                jdbcTemplate.update("UPDATE " + tableName + " SET tunniste = ? where id = ?", uuid, id);
                jdbcTemplate.update("UPDATE " + tableName + "_AUD SET tunniste = ? where id = ?", uuid, id);
            }
        });

    }

}
