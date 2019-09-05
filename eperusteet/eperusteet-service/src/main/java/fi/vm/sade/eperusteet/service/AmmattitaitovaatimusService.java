package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.AmmattitaitovaatimusQueryDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteBaseDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.Ammattitaitovaatimus2019Dto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteKontekstiDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface AmmattitaitovaatimusService {

    /**
     * Puutteellisten ammattitaitovaatimuskoodien liittäminen kohdealueisiin
     */
    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    void addAmmattitaitovaatimuskoodit();

    @PreAuthorize("permitAll()")
    Page<PerusteBaseDto> findPerusteet(PageRequest p, AmmattitaitovaatimusQueryDto pquery);

    @PreAuthorize("permitAll()")
    Page<TutkinnonOsaViiteKontekstiDto> findTutkinnonOsat(PageRequest p, AmmattitaitovaatimusQueryDto pquery);

    void updateAmmattitaitovaatimukset(Long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    List<KoodiDto> addAmmattitaitovaatimuskooditToKoodisto(@P("perusteId") Long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    List<Ammattitaitovaatimus2019Dto> getAmmattitaitovaatimukset(@P("perusteId") Long perusteId);
}
