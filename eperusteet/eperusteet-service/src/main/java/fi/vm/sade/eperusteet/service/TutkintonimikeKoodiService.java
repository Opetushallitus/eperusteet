package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.peruste.TutkintonimikeKoodiDto;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface TutkintonimikeKoodiService {

    @PreAuthorize("hasPermission(#id, 'perusteprojekti', 'LUKU')")
    List<TutkintonimikeKoodiDto> getTutkintonimikekoodit(Long perusteId);
}
