package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;

public class V0_98__tunniste extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        DataSource dataSource = context.getConfiguration().getDataSource();
        migrate(new JdbcTemplate(dataSource));
    }

    public void migrate(final JdbcTemplate jdbcTemplate) throws Exception {
        for (String t : Arrays.asList("yl_keskeinen_sisaltoalue", "yl_laajaalainen_osaaminen", "yl_vlkokonaisuus", "yl_opetuksen_tavoite")) {
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
