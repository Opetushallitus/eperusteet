package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.dto.AmmattitaitovaatimusQueryDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteBaseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
}
