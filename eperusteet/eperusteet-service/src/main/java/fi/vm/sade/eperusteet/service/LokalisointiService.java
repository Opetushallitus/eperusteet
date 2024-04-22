package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.LokalisointiDto;
import fi.vm.sade.eperusteet.dto.util.Lokalisoitava;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Collection;
import java.util.List;

public interface LokalisointiService {

    @PreAuthorize("permitAll()")
    List<LokalisointiDto> getAllByCategoryAndLocale(String category, String locale);

    @PreAuthorize("permitAll()")
    LokalisointiDto get(String key, String locale);

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    void save(List<LokalisointiDto> lokalisoinnit);

    @PreAuthorize("permitAll()")
    <T extends Lokalisoitava, C extends Collection<T>> C lokalisoi(C list);

    @PreAuthorize("permitAll()")
    <T extends Lokalisoitava> T lokalisoi(T list);
}



