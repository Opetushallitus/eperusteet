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

import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiLaajaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 *
 * @author nkala
 */
public interface KoodistoClient {

    @PreAuthorize("permitAll()")
    List<KoodistoKoodiDto> getAll(String koodisto);

    @PreAuthorize("permitAll()")
    KoodistoKoodiDto get(String koodistoUri, String koodiUri);

    @PreAuthorize("permitAll()")
    KoodistoKoodiDto get(String koodistoUri, String koodiUri, Long versio);

    @PreAuthorize("permitAll()")
    List<KoodistoKoodiDto> filterBy(String koodisto, String haku);

    @PreAuthorize("permitAll()")
    List<KoodistoKoodiDto> getAlarelaatio(String koodi);

    @PreAuthorize("permitAll()")
    List<KoodistoKoodiDto> getYlarelaatio(String koodi);

    @PreAuthorize("permitAll()")
    List<KoodistoKoodiDto> getRinnasteiset(String koodi);

    @PreAuthorize("permitAll()")
    KoodistoKoodiLaajaDto getAllByVersio(String koodi, String versio);

    @PreAuthorize("permitAll()")
    KoodistoKoodiDto getLatest(String koodi);

    @PreAuthorize("permitAll()")
    KoodiDto getKoodi(String koodisto, String koodiUri);

    @PreAuthorize("permitAll()")
    KoodiDto getKoodi(String koodisto, String koodiUri, Long versio);

    @PreAuthorize("permitAll()")
    void addNimiAndUri(KoodiDto koodi);
}
