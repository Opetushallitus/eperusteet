/*
 *
 *  *  Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *  *
 *  *  This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  *  soon as they will be approved by the European Commission - subsequent versions
 *  *  of the EUPL (the "Licence");
 *  *
 *  *  You may not use this work except in compliance with the Licence.
 *  *  You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *  *
 *  *  This program is distributed in the hope that it will be useful,
 *  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  *  European Union Public Licence for more details.
 *
 *
 */

package fi.vm.sade.eperusteet.v2.service.impl;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.peruste.PerusteVersionDto;
import fi.vm.sade.eperusteet.dto.peruste.TutkintonimikeKoodiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.TutkintonimikeKoodiRepository;
import fi.vm.sade.eperusteet.service.event.PerusteUpdatedEvent;
import fi.vm.sade.eperusteet.service.event.aop.IgnorePerusteUpdateCheck;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.yl.LukiokoulutuksenPerusteenSisaltoService;
import fi.vm.sade.eperusteet.v2.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.v2.dto.peruste.TutkinnonOsaKaikkiDto;
import fi.vm.sade.eperusteet.v2.service.PerusteServiceV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author isaul
 */
@Service("perusteServiceV2")
@Transactional
public class PerusteServiceV2Impl implements PerusteServiceV2, ApplicationListener<PerusteUpdatedEvent> {

    @Autowired
    private PerusteRepository perusteet;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private TutkintonimikeKoodiRepository tutkintonimikeKoodiRepository;

    @Autowired
    private LukiokoulutuksenPerusteenSisaltoService lukiokoulutuksenPerusteenSisaltoService;

    @Override
    @Transactional(readOnly = true)
    public PerusteKaikkiDto getAmosaaYhteinenPohja() {
        List<Peruste> loydetyt = perusteet.findAllByDiaarinumero(new Diaarinumero("amosaa/yhteiset"));

        if (loydetyt.size() == 1) {
            return getKokoSisalto(loydetyt.get(0).getId());
        } else {
            Optional<Peruste> op = loydetyt.stream()
                    .filter((p) -> p.getVoimassaoloAlkaa() != null)
                    .filter((p) -> p.getVoimassaoloAlkaa().before(new Date()))
                    .sorted(Comparator.comparing(Peruste::getVoimassaoloAlkaa))
                    .findFirst();

            if (op.isPresent()) {
                return getKokoSisalto(op.get().getId());
            }
        }

        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteKaikkiDto getKokoSisalto(final Long id) {
        Peruste peruste = perusteet.findOne(id);
        if (peruste == null) {
            return null;
        }

        PerusteKaikkiDto perusteDto = mapper.map(peruste, PerusteKaikkiDto.class);
        if (peruste.getLukiokoulutuksenPerusteenSisalto() != null) {
            updateLukioKaikkiRakenne(perusteDto, peruste);
        }
        perusteDto.setRevision(perusteet.getLatestRevisionId(id).getNumero());

        if (!perusteDto.getSuoritustavat().isEmpty()
                && perusteDto.getLukiokoulutuksenPerusteenSisalto() == null) {
            perusteDto.setTutkintonimikkeet(getTutkintonimikeKoodit(id));

            Set<TutkinnonOsa> tutkinnonOsat = new LinkedHashSet<>();
            for (Suoritustapa st : peruste.getSuoritustavat()) {
                for (TutkinnonOsaViite t : st.getTutkinnonOsat()) {
                    tutkinnonOsat.add(t.getTutkinnonOsa());
                }
            }
            perusteDto.setTutkinnonOsat(mapper.mapAsList(tutkinnonOsat, TutkinnonOsaKaikkiDto.class));
        }

        return perusteDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutkintonimikeKoodiDto> getTutkintonimikeKoodit(Long perusteId) {
        List<TutkintonimikeKoodi> koodit = tutkintonimikeKoodiRepository.findByPerusteId(perusteId);
        return mapper.mapAsList(koodit, TutkintonimikeKoodiDto.class);
    }

    private void updateLukioKaikkiRakenne(PerusteKaikkiDto perusteDto, Peruste peruste) {
        if (perusteDto.getLukiokoulutuksenPerusteenSisalto() != null) {
            perusteDto.getLukiokoulutuksenPerusteenSisalto().setRakenne(
                    lukiokoulutuksenPerusteenSisaltoService.getOppiaineTreeStructure(peruste.getId()));
        }
        if (perusteDto.getSuoritustavat() != null) {
            // turhia pois:
            perusteDto.getSuoritustavat().forEach(st -> {
                st.setRakenne(null);
                st.setSisalto(null);
                st.setTutkinnonOsat(null);
            });
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteVersionDto getPerusteVersion(final long id) {
        return perusteet.getGlobalPerusteVersion(id);
    }

    @Override
    @IgnorePerusteUpdateCheck
    @Transactional
    @PreAuthorize("hasPermission(#event.perusteId, 'peruste', 'KORJAUS')"
            + " or hasPermission(#event.perusteId, 'peruste', 'MUOKKAUS')"
            + " or hasPermission(#event.perusteId, 'peruste', 'TILANVAIHTO')")
    public void onApplicationEvent(@P("event") PerusteUpdatedEvent event) {
        Peruste peruste = perusteet.findOne(event.getPerusteId());
        if (peruste == null) {
            return;
        }
        Date muokattu = new Date();
        if (peruste.getTila() == PerusteTila.VALMIS) {
            perusteet.setRevisioKommentti("Perusteen sisältöä korjattu");
            peruste.muokattu();
            muokattu = peruste.getMuokattu();
        }
        if (peruste.getGlobalVersion() == null) {
            peruste.setGlobalVersion(new PerusteVersion(peruste));
        }
        peruste.getGlobalVersion().setAikaleima(muokattu);
    }

}
