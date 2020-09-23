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

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.OpasSisalto;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.dto.opas.OpasDto;
import fi.vm.sade.eperusteet.dto.opas.OpasLuontiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKevytDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author nkala
 */
@Transactional
@DirtiesContext
public class OpasServiceIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private PerusteprojektiRepository repository;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteenOsaService perusteenOsaService;

    @Autowired
    private PerusteprojektiService service;

    @Autowired
    private OpasService opasService;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @PersistenceContext
    private EntityManager em;

    private final String ryhmaId = "1.2.246.562.28.11287634288";
    private final LaajuusYksikko yksikko = LaajuusYksikko.OSAAMISPISTE;
    private final String yhteistyotaho = TestUtils.uniikkiString();
    private final String tehtava = TestUtils.uniikkiString();

    private PerusteprojektiDto teePerusteprojekti(PerusteTyyppi tyyppi, String koulutustyyppi) {
        PerusteprojektiLuontiDto ppldto = null;
        if (tyyppi == PerusteTyyppi.NORMAALI) {
            ppldto = new PerusteprojektiLuontiDto(koulutustyyppi, yksikko, null, null, tyyppi, ryhmaId);
            ppldto.setReforminMukainen(false);
            ppldto.setDiaarinumero(TestUtils.uniikkiString());
            ppldto.setYhteistyotaho(yhteistyotaho);
            ppldto.setTehtava(tehtava);
        } else if (tyyppi == PerusteTyyppi.POHJA) {
            ppldto = new PerusteprojektiLuontiDto(koulutustyyppi, null, null, null, tyyppi, ryhmaId);
            ppldto.setReforminMukainen(false);
            ppldto.setYhteistyotaho(yhteistyotaho);
            ppldto.setTehtava(tehtava);
        }

        Assert.assertNotNull(ppldto);

        ppldto.setNimi(TestUtils.uniikkiString());
        return service.save(ppldto);
    }

    private void teeOpas() {
        OpasLuontiDto opasDto = new OpasLuontiDto();
        opasDto.setNimi("Opas 1");
        opasDto.setRyhmaOid(ryhmaId);
        opasService.save(opasDto);
    }

    private void luoProjektit() {
        PerusteprojektiDto ppdto = teePerusteprojekti(PerusteTyyppi.NORMAALI, KoulutusTyyppi.ESIOPETUS.toString());
        Perusteprojekti pp = repository.findOne(ppdto.getId());
        Assert.assertNotNull(pp);

//        PerusteprojektiDto ppdto = teePerusteprojekti(PerusteTyyppi.NORMAALI, KoulutusTyyppi.ESIOPETUS.toString());
//        Perusteprojekti pp = repository.findOne(ppdto.getId());
//        Assert.assertNotNull(pp);
    }

    @Test
    @Rollback(true)
    public void testPerustprojektiluonti12() {
    }

    @Test
    @Rollback(true)
    public void teeOpasprojekti() {
        {
            OpasLuontiDto opasLuontiDto = new OpasLuontiDto();
            opasLuontiDto.setNimi("Opas 1");
            opasLuontiDto.setRyhmaOid(ryhmaId);

            OpasDto opasDto = opasService.save(opasLuontiDto);

            assertThat(opasDto).isNotNull();

            PerusteKaikkiDto perusteKaikki = perusteService.getJulkaistuSisalto(opasDto.getPeruste().getIdLong());
            assertThat(perusteKaikki.getOppaanKoulutustyypit()).isNull();
            assertThat(perusteKaikki.getOppaanPerusteet()).hasSize(0);

            Peruste peruste = perusteRepository.getOne(opasDto.getPeruste().getIdLong());
            assertThat(peruste.getOppaanSisalto().getSisalto()).isNotNull();
            assertThat(peruste.getOppaanSisalto().getSisalto().getLapset()).hasSize(0);
        }

        {
            Peruste p = TestUtils.teePeruste();
            OpasSisalto opasSisalto = new OpasSisalto();
            PerusteenOsaViite viite = new PerusteenOsaViite();
            PerusteenOsaViite viiteLapsi = new PerusteenOsaViite();
            viite.setLapset(Arrays.asList(viiteLapsi));
            opasSisalto.setSisalto(viite);
            p.setSisalto(opasSisalto);
            Peruste pohjaperuste = perusteRepository.save(p);

            OpasLuontiDto opasLuontiDto = new OpasLuontiDto();
            opasLuontiDto.setNimi("Opas 1");
            opasLuontiDto.setRyhmaOid(ryhmaId);
            opasLuontiDto.setPohjaId(p.getId());
            opasLuontiDto.setOppaanKoulutustyypit(Sets.newHashSet(KoulutusTyyppi.PERUSTUTKINTO, KoulutusTyyppi.ERIKOISAMMATTITUTKINTO));
            opasLuontiDto.setOppaanPerusteet(Sets.newHashSet(mapper.map(pohjaperuste, PerusteKevytDto.class)));

            OpasDto opasDto = opasService.save(opasLuontiDto);

            assertThat(opasDto).isNotNull();

            PerusteKaikkiDto perusteKaikki = perusteService.getJulkaistuSisalto(opasDto.getPeruste().getIdLong());
            assertThat(perusteKaikki.getOppaanKoulutustyypit()).containsExactlyInAnyOrder(KoulutusTyyppi.PERUSTUTKINTO, KoulutusTyyppi.ERIKOISAMMATTITUTKINTO);
            assertThat(perusteKaikki.getOppaanPerusteet()).extracting("id").contains(pohjaperuste.getId());

            Peruste peruste = perusteRepository.getOne(opasDto.getPeruste().getIdLong());
            assertThat(peruste.getOppaanSisalto().getSisalto()).isNotNull();
            assertThat(peruste.getOppaanSisalto().getSisalto().getLapset()).hasSize(1);
        }
    }

}
