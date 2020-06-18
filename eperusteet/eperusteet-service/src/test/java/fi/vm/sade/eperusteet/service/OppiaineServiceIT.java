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
package fi.vm.sade.eperusteet.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.yl.VuosiluokkaKokonaisuus;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.peruste.PerusteVersionDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.dto.yl.*;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.VuosiluokkaKokonaisuusRepository;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.TestConfiguration;
import fi.vm.sade.eperusteet.service.yl.OppiaineLockContext;
import fi.vm.sade.eperusteet.service.yl.OppiaineOpetuksenSisaltoTyyppi;
import fi.vm.sade.eperusteet.service.yl.OppiaineService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static fi.vm.sade.eperusteet.service.test.util.TestUtils.*;
import static org.junit.Assert.*;

/**
 *
 * @author jhyoty
 */
@DirtiesContext
public class OppiaineServiceIT extends AbstractIntegrationTest {

    @Autowired
    private VuosiluokkaKokonaisuusRepository vkrepo;
    @Autowired
    private OppiaineService service;
    @Autowired
    @LockCtx(OppiaineLockContext.class)
    private LockService<OppiaineLockContext> lockService;
    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteRepository perusteRepository;

    private Long perusteId;

    @Before
    public void setup() {
        Peruste peruste = perusteService.luoPerusteRunko(KoulutusTyyppi.PERUSOPETUS, null, LaajuusYksikko.OPINTOVIIKKO, PerusteTyyppi.NORMAALI);
        perusteId = peruste.getId();
    }

    @Test
    public void testCRUD() throws IOException {
        VuosiluokkaKokonaisuus vk = new VuosiluokkaKokonaisuus();
        vk = vkrepo.save(vk);

        OppiaineDto oppiaineDto = new OppiaineDto();
        oppiaineDto.setNimi(Optional.of(lt("Oppiaine")));
        oppiaineDto.setTehtava(to("TehtävänOtsikko", "Tehtava"));
        oppiaineDto.setKoosteinen(Optional.of(false));

        OpetuksenKohdealueDto kohdealueDto = new OpetuksenKohdealueDto();
        kohdealueDto.setNimi(olt("Kohdealue"));
        oppiaineDto.setKohdealueet(new HashSet<OpetuksenKohdealueDto>());
        oppiaineDto.getKohdealueet().add(kohdealueDto);

        OppiaineenVuosiluokkaKokonaisuusDto vkDto = new OppiaineenVuosiluokkaKokonaisuusDto();
        vkDto.setTehtava(Optional.of(to("Tehtävä", "")));
        vkDto.setVuosiluokkaKokonaisuus(Optional.of(vk.getReference()));

        oppiaineDto.setVuosiluokkakokonaisuudet(Sets.newHashSet(vkDto));
        KeskeinenSisaltoalueDto ks = new KeskeinenSisaltoalueDto();
        ks.setNimi(Optional.of(lt("Nimi")));
        vkDto.setSisaltoalueet(Lists.newArrayList(ks));
        PerusteVersionDto versionDto = perusteService.getPerusteVersion(perusteId);
        OppiaineDto oa = service.addOppiaine(perusteId, oppiaineDto, OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);
        assertNotEquals(perusteService.getPerusteVersion(perusteId).getAikaleima(), versionDto.getAikaleima());

        OppiaineLockContext lc = new OppiaineLockContext();
        lc.setTyyppi(OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);
        lc.setPerusteId(perusteId);
        lc.setOppiaineId(oa.getId());
        lockService.lock(lc);

        assertEquals("Nimi", oa.getVuosiluokkakokonaisuudet().iterator().next().getSisaltoalueet().get(0).getNimi().get().get(Kieli.FI));
        ks.setNimi(olt("Nimi2"));
        oa.getTehtava().setOtsikko(Optional.of(new LokalisoituTekstiDto(null)));
        oa.getTehtava().setTeksti(Optional.empty());
        oa.getVuosiluokkakokonaisuudet().iterator().next().getSisaltoalueet().add(0, ks);
        oa.getVuosiluokkakokonaisuudet().iterator().next().getSisaltoalueet().get(1).setNimi(null);
        versionDto = perusteService.getPerusteVersion(perusteId);
        oa = service.updateOppiaine(perusteId, new UpdateDto<>(oa), OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);
        assertNotEquals(perusteService.getPerusteVersion(perusteId).getAikaleima(), versionDto.getAikaleima());

        lockService.unlock(lc);

        assertNull(oa.getTehtava().getOtsikko());
        assertNull(oa.getTehtava().getTeksti());
        assertEquals("Nimi2", oa.getVuosiluokkakokonaisuudet().iterator().next().getSisaltoalueet().get(0).getNimi().get().get(Kieli.FI));
        assertEquals("Nimi", oa.getVuosiluokkakokonaisuudet().iterator().next().getSisaltoalueet().get(1).getNimi().get().get(Kieli.FI));

        vkDto = oa.getVuosiluokkakokonaisuudet().iterator().next();
        OpetuksenTavoiteDto tavoiteDto = new OpetuksenTavoiteDto();
        tavoiteDto.setKohdealueet(Collections.singleton(new Reference(oa.getKohdealueet().iterator().next().getId())));
        tavoiteDto.setSisaltoalueet(Collections.singleton(new Reference(vkDto.getSisaltoalueet().get(0).getId())));
        tavoiteDto.setTavoite(olt("Tässäpä jokin kiva tavoite"));
        tavoiteDto.setVapaaTeksti(olt("vapaa teksti kentta"));
        tavoiteDto.setArvioinninkohteet(Sets.newHashSet(
                arvio("Kohde", "osaamisenkuvaus", "hyvakuvaus", 8),
                arvio(null, "osaamisenkuvaus2", "hyvakuvaus2", 9)));

        tavoiteDto.setArvioinninKuvaus(olt("arvioinnin kohde"));
        vkDto.getTavoitteet().add(tavoiteDto);

        lc = OppiaineLockContext.of(OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS, perusteId, oa.getId(), vkDto.getId());
        lockService.lock(lc);
        versionDto = perusteService.getPerusteVersion(perusteId);
        vkDto = service.updateOppiaineenVuosiluokkaKokonaisuus(perusteId, oa.getId(), new UpdateDto<>(vkDto));
        assertNotEquals(perusteService.getPerusteVersion(perusteId).getAikaleima(), versionDto.getAikaleima());

        List<TavoitteenArviointiDto> arvioinnit = new ArrayList<>(vkDto.getTavoitteet().get(0).getArvioinninkohteet()).stream()
                .sorted(Comparator.comparing(arviointi -> arviointi.getArvosana().get()))
                .collect(Collectors.toList());

        assertEquals("Kohde", arvioinnit.get(0).getArvioinninKohde().get().get(Kieli.FI));
        assertEquals("osaamisenkuvaus", arvioinnit.get(0).getOsaamisenKuvaus().get().get(Kieli.FI));
        assertEquals("osaamisenkuvaus", arvioinnit.get(0).getHyvanOsaamisenKuvaus().get().get(Kieli.FI));
        assertEquals(new Integer(8), arvioinnit.get(0).getArvosana().get());

        assertNull(arvioinnit.get(1).getArvioinninKohde());
        assertEquals("osaamisenkuvaus2", arvioinnit.get(1).getOsaamisenKuvaus().get().get(Kieli.FI));
        assertNull(arvioinnit.get(1).getHyvanOsaamisenKuvaus());
        assertEquals(new Integer(9), arvioinnit.get(1).getArvosana().get());

        assertEquals("arvioinnin kohde", vkDto.getTavoitteet().get(0).getArvioinninKuvaus().get().get(Kieli.FI));
        assertEquals("vapaa teksti kentta", vkDto.getTavoitteet().get(0).getVapaaTeksti().get().get(Kieli.FI));

        vkDto.getSisaltoalueet().clear();
        vkDto.getTavoitteet().clear();
        versionDto = perusteService.getPerusteVersion(perusteId);
        vkDto = service.updateOppiaineenVuosiluokkaKokonaisuus(perusteId, oa.getId(), new UpdateDto<>(vkDto));
        assertNotEquals(perusteService.getPerusteVersion(perusteId).getAikaleima(), versionDto.getAikaleima());

        lc = OppiaineLockContext.of(OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS, perusteId, oa.getId(), null);

        List<Revision> revs = service.getOppiaineRevisions(perusteId, oa.getId(), OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);
        lockService.lock(lc);
        oa = service.revertOppiaine(perusteId, oa.getId(), revs.get(2).getNumero(), OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);

        versionDto = perusteService.getPerusteVersion(perusteId);
        service.deleteOppiaine(perusteId, oa.getId(), OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);
        assertNotEquals(perusteService.getPerusteVersion(perusteId).getAikaleima(), versionDto.getAikaleima());
    }

    @Test
    public void testAddAndUpdateOppimaara() throws IOException {
        VuosiluokkaKokonaisuus vk = new VuosiluokkaKokonaisuus();
        vk = vkrepo.save(vk);

        OppiaineDto oppiaineDto = new OppiaineDto();
        oppiaineDto.setNimi(Optional.of(lt("Oppiaine")));
        oppiaineDto.setTehtava(to("TehtävänOtsikko", "Tehtava"));
        oppiaineDto.setKoosteinen(Optional.of(true));

        OppiaineenVuosiluokkaKokonaisuusDto vkDto = new OppiaineenVuosiluokkaKokonaisuusDto();
        vkDto.setTehtava(Optional.of(to("Tehtävä", "")));
        vkDto.setVuosiluokkaKokonaisuus(Optional.of(vk.getReference()));

        oppiaineDto.setVuosiluokkakokonaisuudet(Sets.newHashSet(vkDto));
        KeskeinenSisaltoalueDto ks = new KeskeinenSisaltoalueDto();
        ks.setNimi(Optional.of(lt("Nimi")));
        vkDto.setSisaltoalueet(Lists.newArrayList(ks));
        PerusteVersionDto versionDto = perusteService.getPerusteVersion(perusteId);

        OppiaineDto oa1 = service.addOppiaine(perusteId, oppiaineDto, OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);
        assertNotEquals(perusteService.getPerusteVersion(perusteId).getAikaleima(), versionDto.getAikaleima());
        assertEquals(0, oa1.getOppimaarat().size());

        OppiaineDto oppimaara = new OppiaineDto();
        oppimaara.setNimi(olt("OppimaaranNimi"));
        oppimaara.setOppiaine(Optional.of(new Reference(oa1.getId())));
        vkDto = new OppiaineenVuosiluokkaKokonaisuusDto();
        vkDto.setTehtava(Optional.of(to("Tehtävä", "")));
        vkDto.setVuosiluokkaKokonaisuus(Optional.of(vk.getReference()));
        oppimaara.setVuosiluokkakokonaisuudet(Sets.newHashSet(vkDto));
        versionDto = perusteService.getPerusteVersion(perusteId);
        oppimaara = service.addOppiaine(perusteId, oppimaara, OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);
        assertNotEquals(perusteService.getPerusteVersion(perusteId).getAikaleima(), versionDto.getAikaleima());
        oa1 = service.getOppiaine(perusteId, oa1.getId(), OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);
        assertEquals(1, oa1.getOppimaarat().size());

        OppiaineLockContext lc = OppiaineLockContext.of(OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS, perusteId, oppimaara.getId(), null);
        lockService.lock(lc);
        oppimaara.setTehtava(to("Tehtävä", "Tehtävä"));
        versionDto = perusteService.getPerusteVersion(perusteId);
        oppimaara = service.updateOppiaine(perusteId, new UpdateDto<>(oppimaara), OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);
        assertNotEquals(perusteService.getPerusteVersion(perusteId).getAikaleima(), versionDto.getAikaleima());
        lockService.unlock(lc);
        assertEquals("Tehtävä", oppimaara.getTehtava().getTeksti().get().get(Kieli.FI));
    }

    private static final Logger LOG = LoggerFactory.getLogger(OppiaineServiceIT.class);

    private TavoitteenArviointiDto arvio(String kohde, String kuvaus, String hyvakuvaus, Integer arvosana) {
        TavoitteenArviointiDto arvio = new TavoitteenArviointiDto();
        arvio.setArvioinninKohde(olt(kohde));
        arvio.setOsaamisenKuvaus(olt(kuvaus));
        arvio.setHyvanOsaamisenKuvaus(olt(hyvakuvaus));
        arvio.setArvosana(Optional.of(arvosana));

        return arvio;
    }
}
