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
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.yl.VuosiluokkaKokonaisuus;
import fi.vm.sade.eperusteet.dto.util.EntityReference;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.yl.KeskeinenSisaltoalueDto;
import fi.vm.sade.eperusteet.dto.yl.OpetuksenTavoiteDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineenVuosiluokkaKokonaisuusDto;
import fi.vm.sade.eperusteet.repository.OppiaineRepository;
import fi.vm.sade.eperusteet.repository.PerusopetuksenPerusteenSisaltoRepository;
import fi.vm.sade.eperusteet.repository.VuosiluokkaKokonaisuusRepository;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.yl.OppiaineService;
import java.io.IOException;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import static fi.vm.sade.eperusteet.service.test.util.TestUtils.olt;
import static fi.vm.sade.eperusteet.service.test.util.TestUtils.to;
import static fi.vm.sade.eperusteet.service.test.util.TestUtils.lt;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 *
 * @author jhyoty
 */
@DirtiesContext
public class OppiaineServiceIT extends AbstractIntegrationTest {

    @Autowired
    private OppiaineRepository repo;
    @Autowired
    private VuosiluokkaKokonaisuusRepository vkrepo;
    @Autowired
    private OppiaineService service;
    @Autowired
    private PerusteService perusteService;
    @Autowired
    private PerusopetuksenPerusteenSisaltoRepository sisaltoRepository;

    private Long perusteId;
    @Before
    public void setup() {
        Peruste peruste = perusteService.luoPerusteRunko("koulutustyyppi_9999", LaajuusYksikko.OPINTOVIIKKO, PerusteTila.LUONNOS, PerusteTyyppi.NORMAALI);
        perusteId = peruste.getId();
    }

    @Test
    public void testAddAndUpdate() throws IOException {
        VuosiluokkaKokonaisuus vk = new VuosiluokkaKokonaisuus();
        vk = vkrepo.save(vk);

        OppiaineDto oppiaineDto = new OppiaineDto();
        oppiaineDto.setNimi(Optional.of(lt("Oppiaine")));
        oppiaineDto.setTehtava(Optional.of(to("TehtävänOtsikko","Tehtava")));
        oppiaineDto.setKoosteinen(Optional.of(false));

        OppiaineenVuosiluokkaKokonaisuusDto vkDto = new OppiaineenVuosiluokkaKokonaisuusDto();
        vkDto.setTehtava(Optional.of(to("Tehtävä", "")));
        vkDto.setVuosiluokkaKokonaisuus(Optional.of(vk.getReference()));

        oppiaineDto.setVuosiluokkakokonaisuudet(Sets.newHashSet(vkDto));
        KeskeinenSisaltoalueDto ks = new KeskeinenSisaltoalueDto();
        ks.setNimi(Optional.of(lt("Nimi")));
        vkDto.setSisaltoalueet(Lists.newArrayList(ks));
        OppiaineDto oa = service.addOppiaine(perusteId, oppiaineDto);

        assertEquals("Nimi", oa.getVuosiluokkakokonaisuudet().iterator().next().getSisaltoalueet().get(0).getNimi().get().get(Kieli.FI));
        ks.setNimi(olt("Nimi2"));
        oa.getTehtava().get().setOtsikko(Optional.of(new LokalisoituTekstiDto(null)));
        oa.getTehtava().get().setTeksti(Optional.<LokalisoituTekstiDto>absent());
        oa.getVuosiluokkakokonaisuudet().iterator().next().getSisaltoalueet().add(0, ks);
        oa.getVuosiluokkakokonaisuudet().iterator().next().getSisaltoalueet().get(1).setNimi(null);
        oa = service.updateOppiaine(perusteId, oa);

        assertNull(oa.getTehtava().get().getOtsikko());
        assertNull(oa.getTehtava().get().getTeksti());
        assertEquals("Nimi2", oa.getVuosiluokkakokonaisuudet().iterator().next().getSisaltoalueet().get(0).getNimi().get().get(Kieli.FI));
        assertEquals("Nimi", oa.getVuosiluokkakokonaisuudet().iterator().next().getSisaltoalueet().get(1).getNimi().get().get(Kieli.FI));

        vkDto = oa.getVuosiluokkakokonaisuudet().iterator().next();
        OpetuksenTavoiteDto tavoiteDto = new OpetuksenTavoiteDto();
        tavoiteDto.setSisaltoalueet(Collections.singleton(new EntityReference(vkDto.getSisaltoalueet().get(0).getId())));
        tavoiteDto.setTavoite(olt("Tässäpä jokin kiva tavoite"));
        vkDto.getTavoitteet().add(tavoiteDto);

        vkDto = service.updateOppiaineenVuosiluokkaKokonaisuus(perusteId, oa.getId(), vkDto);

    }


    private static final Logger LOG = LoggerFactory.getLogger(OppiaineServiceIT.class);
}
