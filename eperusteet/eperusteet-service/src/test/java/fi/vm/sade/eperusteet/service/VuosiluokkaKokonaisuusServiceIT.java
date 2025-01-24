package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.peruste.PerusteVersionDto;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.dto.yl.LaajaalainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.yl.VuosiluokkaKokonaisuudenLaajaalainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.yl.VuosiluokkaKokonaisuusDto;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.yl.LaajaalainenOsaaminenService;
import fi.vm.sade.eperusteet.service.yl.PerusopetuksenPerusteenSisaltoService;
import fi.vm.sade.eperusteet.service.yl.VuosiluokkaKokonaisuusContext;
import fi.vm.sade.eperusteet.service.yl.VuosiluokkaKokonaisuusService;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static fi.vm.sade.eperusteet.service.test.util.TestUtils.olt;
import static fi.vm.sade.eperusteet.service.test.util.TestUtils.oto;
import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@DirtiesContext
public class VuosiluokkaKokonaisuusServiceIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteService perusteService;
    @Autowired
    private PerusopetuksenPerusteenSisaltoService sisaltoService;
    @Autowired
    private VuosiluokkaKokonaisuusService service;
    @Autowired
    @LockCtx(VuosiluokkaKokonaisuusContext.class)
    private LockService<VuosiluokkaKokonaisuusContext> lockService;
    @Autowired
    private LaajaalainenOsaaminenService osaaminenService;

    private Long perusteId;
    private Reference osaaminen;

    @Before
    public void setup() {
        Peruste peruste = perusteService.luoPerusteRunko(KoulutusTyyppi.PERUSOPETUS, null, LaajuusYksikko.OPINTOVIIKKO, PerusteTyyppi.NORMAALI);
        perusteId = peruste.getId();
        LaajaalainenOsaaminenDto lo = new LaajaalainenOsaaminenDto();
        lo.setNimi(olt("Nimi"));
        lo = osaaminenService.addLaajaalainenOsaaminen(perusteId, lo);
        osaaminen = new Reference(lo.getId());
    }

    @Test
    public void testAddUpdate() throws IOException {

        VuosiluokkaKokonaisuusDto dto = new VuosiluokkaKokonaisuusDto();
        dto.setNimi(olt("Nimi"));
        dto.setTehtava(oto("Otsikko","Nimi"));
        dto.setSiirtymaEdellisesta(oto("Otsikko","Nimi"));
        dto.setSiirtymaSeuraavaan(oto("Otsikko","Nimi"));
        dto.setLaajaalainenOsaaminen(oto("Otsikko","Nimi"));
        VuosiluokkaKokonaisuudenLaajaalainenOsaaminenDto vlo = new VuosiluokkaKokonaisuudenLaajaalainenOsaaminenDto();
        vlo.setKuvaus(olt("KUVAAUS"));
        vlo.setLaajaalainenOsaaminen(Optional.of(osaaminen));
        dto.setLaajaalaisetOsaamiset(Collections.singleton(vlo));

//        PerusteVersionDto versionDto = perusteService.getPerusteVersion(perusteId);
        dto = service.addVuosiluokkaKokonaisuus(perusteId, dto);
//        assertNotEquals(perusteService.getPerusteVersion(perusteId).getAikaleima(), versionDto.getAikaleima());

        assertEquals(1, dto.getLaajaalaisetOsaamiset().size());
        assertEquals(osaaminen, dto.getLaajaalaisetOsaamiset().iterator().next().getLaajaalainenOsaaminen().get());
        dto.setNimi(olt("Nimi2"));
        dto.getLaajaalaisetOsaamiset().add(vlo);
        final VuosiluokkaKokonaisuusContext ctx = VuosiluokkaKokonaisuusContext.of(perusteId, dto.getId());

        lockService.lock(ctx);
//        versionDto = perusteService.getPerusteVersion(perusteId);
        dto = service.updateVuosiluokkaKokonaisuus(perusteId, new UpdateDto<>(dto));
        assertThat(dto.getNimi().get().get(Kieli.FI)).isEqualTo("Nimi2");
//        assertNotEquals(perusteService.getPerusteVersion(perusteId).getAikaleima(), versionDto.getAikaleima());
        lockService.unlock(ctx);

        assertEquals(2, dto.getLaajaalaisetOsaamiset().size());
//        versionDto = perusteService.getPerusteVersion(perusteId);
        service.deleteVuosiluokkaKokonaisuus(ctx.getPerusteId(), ctx.getKokonaisuusId());
//        assertNotEquals(perusteService.getPerusteVersion(perusteId).getAikaleima(), versionDto.getAikaleima());
        Assertions.assertThatThrownBy(() -> service.getVuosiluokkaKokonaisuus(perusteId, ctx.getKokonaisuusId()))
            .isInstanceOf(BusinessRuleViolationException.class);
    }

}
