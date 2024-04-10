package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

public class V0_20230503101700__julkaisu_dokumentit_fix extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        DataSource dataSource = context.getConfiguration().getDataSource();
        migrate(new JdbcTemplate(dataSource));
    }

    public void migrate(final JdbcTemplate jdbcTemplate) throws Exception {

        List<Integer> perusteIds =  jdbcTemplate.queryForList(
                "select distinct jp.peruste_id "+
                "from julkaistu_peruste jp "+
                "inner join julkaistuperuste_dokumentit jpd on jp.id = jpd.julkaistuperuste_id "+
                "inner join dokumentti dok on dok.id = jpd.dokumentit "+
                "where dok.tila = 'EI_OLE' or dok.dokumenttidata is null",
                Integer.class);

        perusteIds.forEach(perusteId -> {

            Arrays.asList("FI", "SV", "EN").forEach(kieli -> {
                jdbcTemplate.query(
                        "select dok.id, dok.aloitusaika, jd.julkaistuperuste_id "+
                        "from julkaistu_peruste jp "+
                        "inner join julkaistuperuste_dokumentit jd on jp.id = jd.julkaistuperuste_id "+
                        "inner join dokumentti dok on dok.id = jd.dokumentit "+
                        "where jp.peruste_id = ? "+
                        "and dok.kieli = ? "+
                        "and (dok.tila = 'EI_OLE' or dok.dokumenttidata is null) "+
                        "and revision = (select max(revision) from julkaistu_peruste where peruste_id = ?) ",
                        preparedStatement -> {
                            preparedStatement.setLong(1, perusteId);
                            preparedStatement.setString(2, kieli);
                            preparedStatement.setLong(3, perusteId);
                        },
                        (RowCallbackHandler) rs -> {
                            long dokId = rs.getLong(1);
                            String aloitusaika = rs.getString(2);
                            long julkaistuPerusteId = rs.getLong(3);

                            jdbcTemplate.query(
                                    "select id " +
                                        "from dokumentti " +
                                        "where peruste_id = ? "+
                                        "and aloitusaika between timestamp '"+aloitusaika+"' - interval '5 minutes' and timestamp '"+aloitusaika+"' + interval '5 minutes' " +
                                        "and tila = 'VALMIS' " +
                                        "and kieli = ? " +
                                        "and dokumenttidata is not null " +
                                        "limit 1",
                                    preparedStatement -> {
                                        preparedStatement.setLong(1, perusteId);
                                        preparedStatement.setString(2, kieli);
                                    },
                                    (RowCallbackHandler) rs2 -> {
                                        long valmisId = rs2.getLong(1);
                                        jdbcTemplate.update(
                                                "UPDATE julkaistuperuste_dokumentit " +
                                                " SET dokumentit = ? " +
                                                " WHERE dokumentit = ? " +
                                                " AND julkaistuperuste_id = ?",
                                                preparedStatement -> {
                                                    preparedStatement.setLong(1, valmisId);
                                                    preparedStatement.setLong(2, dokId);
                                                    preparedStatement.setLong(3, julkaistuPerusteId);
                                                });
                                    });
                        });
                });
            });

    }
}
