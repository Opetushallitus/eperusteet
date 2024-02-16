package fi.vm.sade.eperusteet.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.security.access.prepost.PreAuthorize;

public interface UlkopuolisetService {

    @PreAuthorize("isAuthenticated()")
    JsonNode getRyhma(String organisaatioOid);

    @PreAuthorize("isAuthenticated()")
    JsonNode getRyhmat();
}
