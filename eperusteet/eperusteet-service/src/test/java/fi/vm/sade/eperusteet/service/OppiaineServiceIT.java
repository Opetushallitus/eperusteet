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

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.yl.VuosiluokkaKokonaisuus;
import fi.vm.sade.eperusteet.dto.peruste.PerusteVersionDto;
import fi.vm.sade.eperusteet.dto.util.EntityReference;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.dto.yl.KeskeinenSisaltoalueDto;
import fi.vm.sade.eperusteet.dto.yl.OpetuksenKohdealueDto;
import fi.vm.sade.eperusteet.dto.yl.OpetuksenTavoiteDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineenVuosiluokkaKokonaisuusDto;
import fi.vm.sade.eperusteet.dto.yl.TavoitteenArviointiDto;
import fi.vm.sade.eperusteet.repository.OppiaineRepository;
import fi.vm.sade.eperusteet.repository.PerusopetuksenPerusteenSisaltoRepository;
import fi.vm.sade.eperusteet.repository.VuosiluokkaKokonaisuusRepository;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.yl.OppiaineLockContext;
import fi.vm.sade.eperusteet.service.yl.OppiaineOpetuksenSisaltoTyyppi;
import fi.vm.sade.eperusteet.service.yl.OppiaineService;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static fi.vm.sade.eperusteet.service.test.util.TestUtils.olt;
import static fi.vm.sade.eperusteet.service.test.util.TestUtils.to;
import static fi.vm.sade.eperusteet.service.test.util.TestUtils.lt;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

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

    private Long perusteId;

    @Before
    public void setup() {
        Peruste peruste = perusteService.luoPerusteRunko(KoulutusTyyppi.PERUSOPETUS, LaajuusYksikko.OPINTOVIIKKO, PerusteTyyppi.NORMAALI);
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
        oa.getTehtava().setTeksti(Optional.<LokalisoituTekstiDto>absent());
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
        tavoiteDto.setKohdealueet(Collections.singleton(new EntityReference(oa.getKohdealueet().iterator().next().getId())));
        tavoiteDto.setSisaltoalueet(Collections.singleton(new EntityReference(vkDto.getSisaltoalueet().get(0).getId())));
        tavoiteDto.setTavoite(olt("Tässäpä jokin kiva tavoite"));
        TavoitteenArviointiDto arvio = new TavoitteenArviointiDto();
        arvio.setArvioinninKohde(olt("Kohde"));
        arvio.setHyvanOsaamisenKuvaus(olt("Kuvaus"));
        tavoiteDto.setArvioinninkohteet(new HashSet<TavoitteenArviointiDto>());
        tavoiteDto.getArvioinninkohteet().add(arvio);
        vkDto.getTavoitteet().add(tavoiteDto);

        lc = OppiaineLockContext.of(OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS, perusteId, oa.getId(), vkDto.getId());
        lockService.lock(lc);
        versionDto = perusteService.getPerusteVersion(perusteId);
        vkDto = service.updateOppiaineenVuosiluokkaKokonaisuus(perusteId, oa.getId(), new UpdateDto<>(vkDto));
        assertNotEquals(perusteService.getPerusteVersion(perusteId).getAikaleima(), versionDto.getAikaleima());

        assertEquals("Kohde", vkDto.getTavoitteet().get(0).getArvioinninkohteet().iterator().next().getArvioinninKohde().get().get(Kieli.FI));

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
        oppimaara.setOppiaine(Optional.of(new EntityReference(oa1.getId())));
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
}
