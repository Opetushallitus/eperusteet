package fi.vm.sade.eperusteet.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

public interface AmosaaClient {

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    List<Object> getTilastot();
}
