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

import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.Tila;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.dto.PerusteDto;
import fi.vm.sade.eperusteet.dto.PerusteInfoDto;
import fi.vm.sade.eperusteet.dto.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.PerusteQuery;
import fi.vm.sade.eperusteet.dto.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.PerusteenSisaltoViiteDto;
import fi.vm.sade.eperusteet.dto.SuoritustapaDto;
import fi.vm.sade.eperusteet.dto.UpdateDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.repository.version.Revision;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 *
 * @author harrik
 */
public interface PerusteService {
    @PreAuthorize("isAuthenticated()")
    void removeTutkinnonOsa(Long id, Suoritustapakoodi of, Long osaId);

    @PreAuthorize("isAuthenticated()")
    TutkinnonOsaViiteDto updateTutkinnonOsa(Long id, Suoritustapakoodi of, TutkinnonOsaViiteDto osa);

    @PreAuthorize("isAuthenticated()")
    TutkinnonOsaViiteDto attachTutkinnonOsa(Long id, Suoritustapakoodi of, TutkinnonOsaViiteDto osa);

    @PreAuthorize("isAuthenticated()")
    TutkinnonOsaViiteDto addTutkinnonOsa(Long id, Suoritustapakoodi suoritustapa, TutkinnonOsaViiteDto osa);

    PerusteDto get(final Long id);

    PerusteKaikkiDto getKokoSisalto(final Long id);

    PerusteDto update(long id, PerusteDto perusteDto);

    PerusteDto getByIdAndSuoritustapa(final Long id, Suoritustapakoodi suoritustapakoodi);

    Page<PerusteDto> getAll(PageRequest page, String kieli);

    List<PerusteInfoDto> getAllInfo();

    Page<PerusteDto> findBy(PageRequest page, PerusteQuery pquery);

    @PreAuthorize("isAuthenticated()")
    PerusteenSisaltoViiteDto addSisalto(final Long perusteId, final Suoritustapakoodi suoritustapakoodi, PerusteenSisaltoViiteDto viite);

    @PreAuthorize("isAuthenticated()")
    PerusteenSisaltoViiteDto addSisaltoLapsi(final Long perusteId, final Long perusteenosaViiteId);

    @PreAuthorize("isAuthenticated()")
    PerusteenSisaltoViiteDto attachSisaltoLapsi(Long perusteId, Long parentViiteId, Long tekstikappaleId);

    @PreAuthorize("isAuthenticated()")
    PerusteenOsaViite addViite(final Long parentId, final Long seuraavaViite, PerusteenOsaViite viite);

    PerusteenOsaViiteDto getSuoritustapaSisalto(final Long perusteId, final Suoritustapakoodi suoritustapakoodi);

    SuoritustapaDto getSuoritustapa(final Long perusteId, final Suoritustapakoodi suoritustapakoodi);

    RakenneModuuliDto getTutkinnonRakenne(final Long perusteId, final Suoritustapakoodi suoritustapa, Integer eTag);

    @PreAuthorize("isAuthenticated()")
    RakenneModuuliDto updateTutkinnonRakenne(final Long perusteId, final Suoritustapakoodi suoritustapa, final UpdateDto<RakenneModuuliDto> rakenne);

    @PreAuthorize("isAuthenticated()")
    RakenneModuuliDto updateTutkinnonRakenne(final Long perusteId, final Suoritustapakoodi suoritustapa, final RakenneModuuliDto rakenne);

    RakenneModuuliDto revertRakenneVersio(Long id, Suoritustapakoodi suoritustapakoodi, Integer versioId);

    List<Revision> getRakenneVersiot(Long id, Suoritustapakoodi suoritustapakoodi);

    RakenneModuuliDto getRakenneVersio(Long id, Suoritustapakoodi suoritustapakoodi, Integer versioId);

    List<TutkinnonOsaViiteDto> getTutkinnonOsat(Long perusteid, Suoritustapakoodi suoritustapakoodi);

    public TutkinnonOsaViiteDto getTutkinnonOsaViite(Long osaId);

    public TutkinnonOsaViiteDto updateTutkinnonOsaViite(Long osaId, TutkinnonOsaViiteDto tov);

    Peruste luoPerusteRunko(String koulutustyyppi, LaajuusYksikko yksikko, Tila tila);

    Peruste luoPerusteRunkoToisestaPerusteesta(Long perusteId);

    Page<PerusteInfoDto> findByInfo(PageRequest page, PerusteQuery pquery);

    @PreAuthorize("isAuthenticated()")
    String lammitys();

    LukkoDto lock(final Long id, Suoritustapakoodi suoritustapakoodi);

    void unlock(final Long id, Suoritustapakoodi suoritustapakoodi);

    LukkoDto getLock(final Long id, Suoritustapakoodi suoritustapakoodi);
}
