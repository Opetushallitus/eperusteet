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
package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.PerusteprojektiInfoDto;
import fi.vm.sade.eperusteet.dto.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.KayttajaprofiiliService;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.util.PerusteenRakenne;
import fi.vm.sade.eperusteet.service.util.PerusteenRakenne.Validointi;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author harrik
 */
@Service
public class PerusteprojektiServiceImpl implements PerusteprojektiService {

    private static final Logger LOG = LoggerFactory.getLogger(PerusteprojektiServiceImpl.class);

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private PerusteprojektiRepository repository;

    @Autowired
    private KayttajaprofiiliService kayttajaprofiiliService;

    @Autowired
    private PerusteService perusteService;

    @Override
    @Transactional(readOnly = true)
    public List<PerusteprojektiInfoDto> getBasicInfo() {
        return mapper.mapAsList(repository.findAll(), PerusteprojektiInfoDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteprojektiDto get(Long id) {
        Perusteprojekti p = repository.findOne(id);
        return mapper.map(p, PerusteprojektiDto.class);
    }

    @Override
    @Transactional(readOnly = false)
    public PerusteprojektiDto save(PerusteprojektiLuontiDto perusteprojektiDto) {
        Perusteprojekti perusteprojekti = mapper.map(perusteprojektiDto, Perusteprojekti.class);

        String koulutustyyppi = perusteprojektiDto.getKoulutustyyppi();
        LaajuusYksikko yksikko = perusteprojektiDto.getLaajuusYksikko();
        PerusteTyyppi tyyppi = perusteprojektiDto.getTyyppi();
        perusteprojekti.setTila(ProjektiTila.LAADINTA);

        if (tyyppi != PerusteTyyppi.POHJA) {
            if (koulutustyyppi.equals("koulutustyyppi_1") && yksikko == null) {
                throw new BusinessRuleViolationException("Opetussuunnitelmalla täytyy olla yksikkö");
            }
            if (perusteprojektiDto.getDiaarinumero() == null) {
                throw new BusinessRuleViolationException("Diaarinumeroa ei ole asetettu");
            }
        }

        Peruste peruste;
        if (perusteprojektiDto.getPerusteId() == null) {
            peruste = perusteService.luoPerusteRunko(koulutustyyppi, yksikko, PerusteTila.LUONNOS, tyyppi);
        }
        else {
            peruste = perusteService.luoPerusteRunkoToisestaPerusteesta(perusteprojektiDto.getPerusteId(), tyyppi);
        }

        perusteprojekti.setPeruste(peruste);
        perusteprojekti = repository.save(perusteprojekti);
        kayttajaprofiiliService.addPerusteprojekti(perusteprojekti.getId());

        return mapper.map(perusteprojekti, PerusteprojektiDto.class);
    }

    @Override
    @Transactional(readOnly = false)
    public PerusteprojektiDto update(Long id, PerusteprojektiDto perusteprojektiDto) {
        Perusteprojekti vanhaProjekti = repository.findOne(id);
        if (vanhaProjekti == null) {
            throw new BusinessRuleViolationException("Projektia ei ole olemassa id:llä: " + id);
        }

        perusteprojektiDto.setId(id);
        perusteprojektiDto.setTila(vanhaProjekti.getTila());
        Perusteprojekti perusteprojekti = mapper.map(perusteprojektiDto, Perusteprojekti.class);
        perusteprojekti = repository.save(perusteprojekti);
        return mapper.map(perusteprojekti, PerusteprojektiDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<ProjektiTila> getTilat(Long id) {
        Perusteprojekti p = repository.findOne(id);
        if (p == null) {
            throw new BusinessRuleViolationException("Projektia ei ole olemassa id:llä: " + id);
        }

        return p.getTila().mahdollisetTilat(p.getPeruste().getTyyppi());
    }

    @Override
    @Transactional(readOnly = false)
    public TilaUpdateStatus updateTila(Long id, ProjektiTila tila) {
        TilaUpdateStatus updateStatus = new TilaUpdateStatus();
        updateStatus.setVaihtoOk(true);

        Perusteprojekti projekti = repository.findOne(id);

        if (projekti == null) {
            throw new BusinessRuleViolationException("Projektia ei ole olemassa id:llä: " + id);
        }
        updateStatus.setVaihtoOk(projekti.getTila().mahdollisetTilat(projekti.getPeruste().getTyyppi()).contains(tila));
        if ( !updateStatus.isVaihtoOk() ) {
            String viesti = "Tilasiirtymä tilasta '" + projekti.getTila().toString() + "' tilaan '" + tila.toString() + "' ei mahdollinen";
            updateStatus.addStatus(viesti);
            return updateStatus;
        }

        if (projekti.getPeruste() != null && projekti.getPeruste().getSuoritustavat() != null
            && tila == ProjektiTila.VIIMEISTELY && projekti.getTila() == ProjektiTila.LAADINTA) {
            Validointi validointi;
            for (Suoritustapa suoritustapa : projekti.getPeruste().getSuoritustavat()) {
                if (suoritustapa.getRakenne() != null) {
                    validointi = PerusteenRakenne.validoiRyhma(suoritustapa.getRakenne());
                    if (!validointi.ongelmat.isEmpty()) {
                        updateStatus.addStatus("Rakenteen validointi virhe", suoritustapa.getSuoritustapakoodi(), validointi);
                        updateStatus.setVaihtoOk(false);
                    }
                }

                List<TutkinnonOsaViite> vapaatOsat = vapaatTutkinnonosat(suoritustapa);
                if (!vapaatOsat.isEmpty()) {
                    List<LokalisoituTekstiDto> nimet = new ArrayList<>();
                    for (TutkinnonOsaViite viite : vapaatOsat) {
                        if (viite.getTutkinnonOsa().getNimi() != null) {
                           nimet.add(new LokalisoituTekstiDto(viite.getTutkinnonOsa().getNimi().getId(), viite.getTutkinnonOsa().getNimi().getTeksti()));
                        }
                    }
                    updateStatus.addStatus("liittamattomia-tutkinnon-osia", suoritustapa.getSuoritustapakoodi(), nimet);
                    updateStatus.setVaihtoOk(false);
                }
            }
        }

        if ( !updateStatus.isVaihtoOk() ) {
            return updateStatus;
        }

        if (tila == ProjektiTila.JULKAISTU && projekti.getTila() == ProjektiTila.VALMIS) {
            setPerusteTila(projekti.getPeruste(), PerusteTila.VALMIS);
        }

        if (tila == ProjektiTila.POISTETTU) {
            projekti.getPeruste().setTila(PerusteTila.POISTETTU);
        }
        
        if (tila == ProjektiTila.LAADINTA && projekti.getTila() == ProjektiTila.POISTETTU) {
            setPerusteTila(projekti.getPeruste(), PerusteTila.LUONNOS);
        }
        
        if (projekti.getPeruste().getTyyppi() == PerusteTyyppi.POHJA && tila == ProjektiTila.VALMIS
            && projekti.getTila() == ProjektiTila.LAADINTA) {
            setPerusteTila(projekti.getPeruste(), PerusteTila.VALMIS);
        }

        projekti.setTila(tila);
        repository.save(projekti);
        return updateStatus;
    }

    private void setPerusteTila(Peruste peruste, PerusteTila tila) {
        for (Suoritustapa suoritustapa : peruste.getSuoritustavat()) {
                setSisaltoTila(suoritustapa.getSisalto(), tila);
                for (TutkinnonOsaViite tutkinnonosaViite : suoritustapa.getTutkinnonOsat()) {
                    setOsatTila(tutkinnonosaViite, tila);
                }
            }
            peruste.setTila(tila);
    }
            
    private PerusteenOsaViite setSisaltoTila(PerusteenOsaViite sisaltoRoot, PerusteTila tila) {
        if (sisaltoRoot.getPerusteenOsa() != null) {
            sisaltoRoot.getPerusteenOsa().setTila(tila);
        }
        if (sisaltoRoot.getLapset() != null) {
            for (PerusteenOsaViite lapsi : sisaltoRoot.getLapset()) {
               setSisaltoTila(lapsi, tila);
            }
        }
        return sisaltoRoot;
    }

    private TutkinnonOsaViite setOsatTila(TutkinnonOsaViite osa, PerusteTila tila) {
        if (osa.getTutkinnonOsa()!= null) {
            osa.getTutkinnonOsa().setTila(tila);
        }
        return osa;
    }


    private List<TutkinnonOsaViite> vapaatTutkinnonosat(Suoritustapa suoritustapa) {
        List<TutkinnonOsaViite> viiteList = new ArrayList<>();

        RakenneModuuli rakenne = suoritustapa.getRakenne();
        if (rakenne != null) {
            for (TutkinnonOsaViite viite : suoritustapa.getTutkinnonOsat()) {
                if (!rakenne.isInRakenne(viite, true) && (viite.getPoistettu() == null || !viite.getPoistettu())) {
                    viiteList.add(viite);
                }
            }
        }
        return viiteList;
    }

}
