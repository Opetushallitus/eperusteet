package fi.vm.sade.eperusteet.domain.tutkinnonosa;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class OsaAlueTest {

    @Test
    public void testOsaAlueCopyConstructor() {
        OsaAlue oa1 = new OsaAlue();
        Osaamistavoite ot = new Osaamistavoite();
        ot.setPakollinen(true);
        Osaamistavoite ot2 = new Osaamistavoite();
        ot2.setPakollinen(false);
        ot2.setEsitieto(ot);
        List<Osaamistavoite> tavoitteet = new ArrayList<>();
        tavoitteet.add(ot2);
        tavoitteet.add(ot);
        oa1.setOsaamistavoitteet(tavoitteet);

        OsaAlue oa2 = new OsaAlue(oa1);
        assertTrue(oa2.getOsaamistavoitteet().size() == 2);
        assertTrue(oa2.getOsaamistavoitteet().get(0).getEsitieto() == oa2.getOsaamistavoitteet().get(1));
    }

}
