package db.migration;

import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;

public class V0_161__tutkinnon_rakenne_tunniste_paivitys implements SpringJdbcMigration {
    @Override
    public void migrate(final JdbcTemplate jdbcTemplate) throws Exception {
        for (String t : Arrays.asList("tutkinnon_rakenne")) {
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
