package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.dto.peruste.PerusteVersionDto;
import fi.vm.sade.eperusteet.dto.yl.LaajaalainenOsaaminenDto;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.yl.LaajaalainenOsaaminenContext;
import fi.vm.sade.eperusteet.service.yl.LaajaalainenOsaaminenService;
import fi.vm.sade.eperusteet.service.yl.PerusopetuksenPerusteenSisaltoService;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import static fi.vm.sade.eperusteet.service.test.util.TestUtils.olt;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@DirtiesContext
public class PerusopetuksenSisaltoServiceIT extends AbstractIntegrationTest {

    @Autowired
    private PerusopetuksenPerusteenSisaltoService service;
    @Autowired
    private PerusteService perusteService;
    @Autowired
    private LaajaalainenOsaaminenService osaaminenService;
    @Autowired
    @LockCtx(LaajaalainenOsaaminenContext.class)
    private LockService<LaajaalainenOsaaminenContext> lockService;

    private Long perusteId;
    @Before
    public void setup() {
        Peruste peruste = perusteService.luoPerusteRunko(KoulutusTyyppi.PERUSOPETUS, null, LaajuusYksikko.OPINTOVIIKKO, PerusteTyyppi.NORMAALI);
        perusteId = peruste.getId();
    }

    @Test
    public void testUpdateLaajaalaiset() throws IOException {
        LaajaalainenOsaaminenDto lo = new LaajaalainenOsaaminenDto();
        lo.setNimi(olt("Nimi"));
        PerusteVersionDto versionDto = perusteService.getPerusteVersion(perusteId);
        lo = osaaminenService.addLaajaalainenOsaaminen(perusteId, lo);
        assertNotEquals(perusteService.getPerusteVersion(perusteId).getAikaleima(), versionDto.getAikaleima());
        assertEquals(1, service.getLaajaalaisetOsaamiset(perusteId).size());
        lo.setNimi(null);
        lo.setKuvaus(olt("Kuvaus"));

        final LaajaalainenOsaaminenContext ctx = LaajaalainenOsaaminenContext.of(perusteId, lo.getId());
        lockService.lock(ctx);
        versionDto = perusteService.getPerusteVersion(perusteId);
        lo = osaaminenService.updateLaajaalainenOsaaminen(perusteId, lo);
        assertNotEquals(perusteService.getPerusteVersion(perusteId).getAikaleima(), versionDto.getAikaleima());
        lockService.unlock(ctx);
        assertEquals("Kuvaus", lo.getKuvaus().get().get(Kieli.FI));
        assertEquals("Nimi", lo.getNimi().get().get(Kieli.FI));
    }

}
