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

package fi.vm.sade.eperusteet.domain.tutkinnonosa;

import fi.vm.sade.eperusteet.domain.tutkinnonOsa.OsaAlue;
import fi.vm.sade.eperusteet.domain.tutkinnonOsa.Osaamistavoite;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author jhyoty
 */
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
