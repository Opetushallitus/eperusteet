package fi.vm.sade.eperusteet.service.internal;

import fi.vm.sade.eperusteet.domain.Lukko;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.service.exception.LockingException;

public interface LockManager {
    Lukko lock(Long id);
    void lisaaNimiLukkoon(LukkoDto lock);
    boolean isLockedByAuthenticatedUser(Long id);
    /**
     * Varmistaa että tunnistettu käyttäjä omistaa lukon,
     * @param id lukon tunniste
     * @throws LockingException jos lukkoa ei ole tai sen omistaa toinen käyttäjä
     */
    void ensureLockedByAuthenticatedUser(Long id);
    Lukko getLock(Long id);
    boolean unlock(Long id);
}
