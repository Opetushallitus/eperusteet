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

import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.dto.opas.OpasLuontiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author nkala
 */
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class OpasServiceIT {

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
            ppldto.setDiaarinumero(TestUtils.uniikkiString());
            ppldto.setYhteistyotaho(yhteistyotaho);
            ppldto.setTehtava(tehtava);
        } else if (tyyppi == PerusteTyyppi.POHJA) {
            ppldto = new PerusteprojektiLuontiDto(koulutustyyppi, null, null, null, tyyppi, ryhmaId);
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
        luoProjektit();
    }

    @Test
    @Rollback(true)
    public void teeOpasprojekti() {
    }

}
