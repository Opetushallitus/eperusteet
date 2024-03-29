/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.service;
import fi.vm.sade.eperusteet.dto.LokalisointiDto;
import fi.vm.sade.eperusteet.dto.util.Lokalisoitava;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author jussi
 */
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



