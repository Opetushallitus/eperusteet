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

import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.Sortable;
import fi.vm.sade.eperusteet.dto.SortableDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 *
 * @author harrik
 */
public interface PerusteenOsaViiteService {

    @PreAuthorize("hasPermission(#perusteId,'peruste','MUOKKAUS')")
    PerusteenOsaViiteDto.Laaja kloonaaTekstiKappale(@P("perusteId") Long perusteId, Long id);

    @PreAuthorize("hasPermission(#perusteId,'peruste','MUOKKAUS')")
    TutkinnonOsaViiteDto kloonaaTutkinnonOsa(@P("perusteId") Long perusteId, Suoritustapakoodi tapa,Long id);

    @PreAuthorize("hasPermission(#perusteId,'peruste','MUOKKAUS')")
    void reorderSubTree(@P("perusteId") Long perusteId, Long rootViiteId, PerusteenOsaViiteDto.Puu<?,?> uusi);

    @PreAuthorize("hasPermission(#perusteId,'peruste','LUKU')")
    <T extends PerusteenOsaViiteDto<?>> T getSisalto(@P("perusteId") Long perusteId, Long viiteId, Class<T> view);

    @PreAuthorize("hasPermission(#perusteId,'peruste','MUOKKAUS')")
    void removeSisalto(@P("perusteId") Long perusteId, Long id);

    @PreAuthorize("hasPermission(#perusteId,'peruste','MUOKKAUS')")
    PerusteenOsaViiteDto.Matala addSisalto(@P("perusteId") Long perusteId, Long viiteId, PerusteenOsaViiteDto.Matala viiteDto);

    // Käytetään ohjelmallisesti puuttuvien sisältöjen lisäämiseen
    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    PerusteenOsaViiteDto.Matala addSisaltoJulkaistuun(@P("perusteId") Long perusteId, Long viiteId, PerusteenOsaViiteDto.Matala viiteDto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    <T extends Sortable> List<T> sort(@P("perusteId") Long id, Suoritustapakoodi suoritustapakoodi, List<T> sorted);
}
