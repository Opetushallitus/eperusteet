package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Kieli;
import org.springframework.security.access.prepost.PreAuthorize;

public interface LocalizedMessagesService {

    @PreAuthorize("permitAll()")
    String translate(String key, Kieli kieli);

}
