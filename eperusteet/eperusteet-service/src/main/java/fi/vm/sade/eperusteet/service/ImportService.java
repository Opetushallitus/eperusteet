package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiImportDto;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import org.springframework.security.access.prepost.PreAuthorize;

public interface ImportService {
    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    default PerusteprojektiDto tuoPerusteprojekti(PerusteprojektiImportDto projekti) {
        throw new BusinessRuleViolationException("ei-tuettu");
    }
}
