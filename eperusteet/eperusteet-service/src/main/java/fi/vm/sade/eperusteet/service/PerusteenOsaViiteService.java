package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.Sortable;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

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

    @PreAuthorize("hasPermission(#perusteId,'peruste','MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    PerusteenOsaViiteDto.Matala addSisalto(@P("perusteId") Long perusteId, Long viiteId, PerusteenOsaViiteDto.Matala viiteDto);

    // Käytetään ohjelmallisesti puuttuvien sisältöjen lisäämiseen
    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    PerusteenOsaViiteDto.Matala addSisaltoJulkaistuun(@P("perusteId") Long perusteId, Long viiteId, PerusteenOsaViiteDto.Matala viiteDto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    <T extends Sortable> List<T> sort(@P("perusteId") Long id, Suoritustapakoodi suoritustapakoodi, List<T> sorted);
}
