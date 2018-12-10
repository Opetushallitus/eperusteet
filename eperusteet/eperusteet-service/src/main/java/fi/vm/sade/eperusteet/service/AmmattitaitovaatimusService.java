package fi.vm.sade.eperusteet.service;

import org.springframework.security.access.prepost.PreAuthorize;

public interface AmmattitaitovaatimusService {

    /**
     * Puutteellisten ammattitaitovaatimuskoodien liittäminen kohdealueisiin
     */
    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    void addAmmattitaitovaatimuskoodit();

}
