package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.kayttaja.KayttajaProfiiliDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajaprofiiliPreferenssiDto;
import fi.vm.sade.eperusteet.dto.kayttaja.SuosikkiDto;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext
public class KayttajaprofiiliServiceIT extends AbstractIntegrationTest {

    @Autowired
    KayttajaprofiiliService service;

    @Test
    public void testKayttajaprofiili() {
        KayttajaProfiiliDto kp = service.addSuosikki(new SuosikkiDto(null, "jotain", "juttua", new Date()));
        Assert.assertNotNull(kp);
        Assert.assertEquals(1, kp.getSuosikit().size());

        kp = service.setPreference(new KayttajaprofiiliPreferenssiDto("avain", "arvo"));
        Assert.assertEquals(1, kp.getPreferenssit().size());

        KayttajaprofiiliPreferenssiDto newPreference = kp.getPreferenssit().get(0);
        Assert.assertEquals("avain", newPreference.getAvain());
        Assert.assertEquals("arvo", newPreference.getArvo());
        Assert.assertNotNull(newPreference.getId());

        kp = service.setPreference(new KayttajaprofiiliPreferenssiDto("avain", "toinen"));
        KayttajaprofiiliPreferenssiDto oldPreference = kp.getPreferenssit().get(0);
        Assert.assertEquals("avain", oldPreference.getAvain());
        Assert.assertEquals("toinen", oldPreference.getArvo());
        Assert.assertNotNull(oldPreference.getId());
    }
}
