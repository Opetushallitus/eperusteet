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

package fi.vm.sade.eperusteet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;

/**
 *
 * @author nkala
 */
@Repository
public interface TutkinnonOsaRepository extends JpaWithVersioningRepository<TutkinnonOsa, Long> {
    List<TutkinnonOsa> findByKoodiUri(String koodiUri);
    List<TutkinnonOsa> findByNimiTekstiTekstiContainingIgnoreCase(String teksti);

    @Query("SELECT to FROM Peruste p JOIN p.suoritustavat s JOIN s.tutkinnonOsat t JOIN t.tutkinnonOsa to WHERE to.koodi.uri = :koodiUri AND p.tila = 'VALMIS' ")
    List<TutkinnonOsa> findByKoodiUriAndValmiitPerusteet(@Param("koodiUri") String koodiUri);
}
