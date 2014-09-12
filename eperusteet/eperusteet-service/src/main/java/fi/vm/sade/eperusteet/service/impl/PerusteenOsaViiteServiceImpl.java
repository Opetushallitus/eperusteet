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

import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.tutkinnonOsa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaViiteRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaViiteRepository;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.internal.ArviointiService;
import fi.vm.sade.eperusteet.service.PerusteenOsaService;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author harrik
 */
@Service
@Transactional(readOnly = true)
public class PerusteenOsaViiteServiceImpl implements PerusteenOsaViiteService {

    @Autowired
    private ArviointiService arviointiService;

    @PersistenceContext
    private EntityManager em;
    @Autowired
    @Dto
    private DtoMapper mapper;
    @Autowired
    private PerusteenOsaRepository perusteenOsaRepository;

    @Autowired
    private PerusteenOsaService perusteenOsaService;
    @Autowired
    private PerusteenOsaViiteRepository repository;

    @Autowired
    private TutkinnonOsaRepository tutkinnonOsaRepository;

    @Autowired
    private TutkinnonOsaViiteRepository tutkinnonOsaViiteRepository;

    @Override
    public PerusteenOsaViiteDto.Laaja getVersio(Long id, Integer versioId) {
        return mapper.map(repository.findRevision(id, versioId), fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto.Laaja.class);
    }

    @Override
    public List<Revision> getVersiot(Long id) {
        return repository.getRevisions(id);
    }

    @Override
    @Transactional(readOnly = false)
    public PerusteenOsaViiteDto.Laaja kloonaaTekstiKappale(Long id) {
        PerusteenOsaViite pov = repository.findOne(id);
        PerusteenOsa from = pov.getPerusteenOsa();
        if (from instanceof TekstiKappale) {

            TekstiKappale uusi = new TekstiKappale();
            uusi.setTila(PerusteTila.LUONNOS);
            uusi.setNimi(from.getNimi());
            uusi.setTeksti(((TekstiKappale) from).getTeksti());
            pov.setPerusteenOsa(perusteenOsaRepository.save(uusi));
        }
        return mapper.map(pov, fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto.Laaja.class);
    }

    @Override
    @Transactional(readOnly = false)
    public TutkinnonOsaViiteDto kloonaaTutkinnonOsa(Long id) {
        TutkinnonOsaViite tov = tutkinnonOsaViiteRepository.getOne(id);
        TutkinnonOsa to = tov.getTutkinnonOsa();
        TutkinnonOsa uusi = new TutkinnonOsa();
        uusi.setTila(PerusteTila.LUONNOS);
        uusi.setNimi(to.getNimi());
        uusi.setAmmattitaidonOsoittamistavat(to.getAmmattitaidonOsoittamistavat());
        uusi.setAmmattitaitovaatimukset(to.getAmmattitaitovaatimukset());
        uusi.setArviointi(arviointiService.kopioi(to.getArviointi()));
        uusi.setOpintoluokitus(to.getOpintoluokitus());
        uusi.setTavoitteet(to.getTavoitteet());
        tov.setTutkinnonOsa(tutkinnonOsaRepository.save(uusi));
        return mapper.map(tov, TutkinnonOsaViiteDto.class);
    }

    @Override
    @Transactional
    public void removeSisalto(Long id) {
        PerusteenOsaViite viite = repository.findOne(id);
        if (viite == null) {
            throw new BusinessRuleViolationException("Perusteenosaviitettä ei ole olemassa");
        }

        if (viite.getVanhempi() == null) {
            throw new BusinessRuleViolationException("Suoritustavan juurielementtiä ei voi poistaa");
        }

        if (viite.getLapset() != null && !viite.getLapset().isEmpty()) {
            throw new BusinessRuleViolationException("Sisällöllä on lapsia, ei voida poistaa");
        }

        if (viite.getPerusteenOsa() != null && viite.getPerusteenOsa().getTila().equals(PerusteTila.LUONNOS)) {
            PerusteenOsa perusteenOsa = viite.getPerusteenOsa();
            perusteenOsaService.delete(perusteenOsa.getId());

            viite.setPerusteenOsa(null);
            viite.getVanhempi().getLapset().remove(viite);
            viite.setVanhempi(null);
            repository.delete(viite);
        }
    }

    @Override
    @Transactional
    public PerusteenOsaViiteDto.Laaja revertToVersio(Long id, Integer versioId) {
        PerusteenOsaViite revision = repository.findRevision(id, versioId);
        return mapper.map(repository.save(revision), fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto.Laaja.class);
    }

    @Override
    @Transactional
    public void update(Long id, PerusteenOsaViiteDto<?> uusi) {
        PerusteenOsaViite viite = repository.getOne(id);
        clearChildren(viite);
        PerusteenOsaViite parent = viite.getVanhempi();
        updateTraverse(parent, uusi);
    }

    private void clearChildren(PerusteenOsaViite pov) {
        for (PerusteenOsaViite lapsi : pov.getLapset()) {
            clearChildren(lapsi);
        }
        pov.setVanhempi(null);
        pov.getLapset().clear();
    }

    private PerusteenOsaViite updateTraverse(PerusteenOsaViite parent, PerusteenOsaViiteDto<?> uusi) {
        PerusteenOsaViite pov = repository.getOne(uusi.getId());
        pov.setVanhempi(parent);

        List<PerusteenOsaViite> lapset = pov.getLapset();
        lapset.clear();

        for (PerusteenOsaViiteDto<?> x : uusi.getLapset()) {
            lapset.add(updateTraverse(pov, x));
        }
        return repository.save(pov);
    }
}
