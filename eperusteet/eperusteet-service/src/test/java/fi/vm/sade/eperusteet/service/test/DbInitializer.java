package fi.vm.sade.eperusteet.service.test;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Alustaa tyhjän tietokannan integraatiotestejä varten.
 */
public class DbInitializer {

    @Autowired
    private Flyway flyway;

    public DbInitializer() {
        //NOP
    }

    public void initDb() {
        flyway.clean();
        flyway.migrate();
    }

}
