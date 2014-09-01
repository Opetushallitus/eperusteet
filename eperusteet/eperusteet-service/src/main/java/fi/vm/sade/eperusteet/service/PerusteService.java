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
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteInfoDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteQuery;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenSisaltoViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.SuoritustapaDto;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.repository.version.Revision;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 *
 * @author harrik
 */
public interface PerusteService {
    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void removeTutkinnonOsa(@P("perusteId") Long perusteId, Suoritustapakoodi of, Long osaId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    TutkinnonOsaViiteDto updateTutkinnonOsa(@P("perusteId") Long perusteId, Suoritustapakoodi of, TutkinnonOsaViiteDto osa);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    TutkinnonOsaViiteDto attachTutkinnonOsa(@P("perusteId") Long perusteId, Suoritustapakoodi of, TutkinnonOsaViiteDto osa);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    TutkinnonOsaViiteDto addTutkinnonOsa(@P("perusteId") Long perusteId, Suoritustapakoodi suoritustapa, TutkinnonOsaViiteDto osa);

    PerusteDto get(final Long id);

    PerusteKaikkiDto getKokoSisalto(final Long id);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    PerusteDto update(@P("perusteId") long perusteId, PerusteDto perusteDto);

    PerusteDto getByIdAndSuoritustapa(final Long id, Suoritustapakoodi suoritustapakoodi);

    Page<PerusteDto> getAll(PageRequest page, String kieli);

    List<PerusteInfoDto> getAllInfo();

    Page<PerusteDto> findBy(PageRequest page, PerusteQuery pquery);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    PerusteenSisaltoViiteDto addSisalto(@P("perusteId") final Long perusteId, final Suoritustapakoodi suoritustapakoodi, PerusteenSisaltoViiteDto viite);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    PerusteenSisaltoViiteDto addSisaltoLapsi(@P("perusteId") final Long perusteId, final Long perusteenosaViiteId);

    @PreAuthorize("isAuthenticated()")
    PerusteenSisaltoViiteDto attachSisaltoLapsi(Long perusteId, Long parentViiteId, Long tekstikappaleId);

    @PreAuthorize("isAuthenticated()")
    PerusteenOsaViite addViite(final Long parentId, final Long seuraavaViite, PerusteenOsaViite viite);

    PerusteenOsaViiteDto getSuoritustapaSisalto(final Long perusteId, final Suoritustapakoodi suoritustapakoodi);

    SuoritustapaDto getSuoritustapa(final Long perusteId, final Suoritustapakoodi suoritustapakoodi);

    RakenneModuuliDto getTutkinnonRakenne(final Long perusteId, final Suoritustapakoodi suoritustapa, Integer eTag);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    RakenneModuuliDto updateTutkinnonRakenne(@P("perusteId") final Long perusteId, final Suoritustapakoodi suoritustapa, final UpdateDto<RakenneModuuliDto> rakenne);
    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    RakenneModuuliDto updateTutkinnonRakenne(@P("perusteId") final Long perusteId, final Suoritustapakoodi suoritustapa, final RakenneModuuliDto rakenne);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    RakenneModuuliDto revertRakenneVersio(@P("perusteId") Long id, Suoritustapakoodi suoritustapakoodi, Integer versioId);

    List<Revision> getRakenneVersiot(Long id, Suoritustapakoodi suoritustapakoodi);

    RakenneModuuliDto getRakenneVersio(Long id, Suoritustapakoodi suoritustapakoodi, Integer versioId);

    List<TutkinnonOsaViiteDto> getTutkinnonOsat(Long perusteid, Suoritustapakoodi suoritustapakoodi);

    Peruste luoPerusteRunko(String koulutustyyppi, LaajuusYksikko yksikko, PerusteTila tila, PerusteTyyppi tyyppi);

    Peruste luoPerusteRunkoToisestaPerusteesta(Long perusteId, PerusteTyyppi tyyppi);

    Page<PerusteInfoDto> findByInfo(PageRequest page, PerusteQuery pquery);

    @PreAuthorize("isAuthenticated()")
    String lammitys();

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    LukkoDto lock(@P("perusteId") final Long id, Suoritustapakoodi suoritustapakoodi);

    void unlock(final Long id, Suoritustapakoodi suoritustapakoodi);
    LukkoDto getLock(final Long id, Suoritustapakoodi suoritustapakoodi);

    public Map<Long, LukkoDto> getLocksTutkinnonOsat(Long id, Suoritustapakoodi suoritustapakoodi);

    public Map<Long, LukkoDto> getLocksPerusteenOsat(Long id, Suoritustapakoodi suoritustapakoodi);
}
