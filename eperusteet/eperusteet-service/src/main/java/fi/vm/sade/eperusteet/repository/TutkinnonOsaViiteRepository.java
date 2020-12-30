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

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author jhyoty
 */
public interface TutkinnonOsaViiteRepository extends JpaWithVersioningRepository<TutkinnonOsaViite, Long> {
    @Query("SELECT COUNT(tov) FROM TutkinnonOsaViite tov WHERE tov.tutkinnonOsa = ?1")
    long perusteUsageAmount(TutkinnonOsa tosa);

    @Query("SELECT CASE COUNT(*) WHEN 0 THEN false ELSE true END FROM RakenneOsa r WHERE r.tutkinnonOsaViite = ?1")
    boolean isInUse(TutkinnonOsaViite viite);

    @Query("SELECT v FROM Peruste p JOIN p.suoritustavat s JOIN s.tutkinnonOsat v JOIN FETCH v.tutkinnonOsa WHERE p.id = ?1 AND s.suoritustapakoodi = ?2")
    List<TutkinnonOsaViite> findByPeruste(Long perusteId, Suoritustapakoodi st);

    Long countByTutkinnonOsaId(Long perusteenOsaId);

    List<TutkinnonOsaViite> findAllByTutkinnonOsa(TutkinnonOsa perusteenOsa);

    @Query("SELECT tov FROM TutkinnonOsaViite tov WHERE tov.tutkinnonOsa.koodi.uri = ?1 AND tov.suoritustapa.suoritustapakoodi = ?2")
    TutkinnonOsaViite findOneByKoodiUri(String koodiUri, Suoritustapakoodi st);

    String findByPerusteAndNimiWhere = "WHERE (:perusteId = 0l or p.id = :perusteId) " +
            "AND (:nimi IS NULL or LOWER(teksti.teksti) LIKE LOWER(CONCAT('%',:nimi,'%')))" +
            "AND teksti.kieli = :kieli " +
            "AND (:vanhentuneet = true OR (:vanhentuneet = false AND (p.voimassaoloLoppuu IS NULL OR p.voimassaoloLoppuu > NOW())))";

    @Query(value = "SELECT v " +
            "FROM Peruste p " +
            "JOIN p.suoritustavat s " +
            "JOIN s.tutkinnonOsat v " +
            "JOIN FETCH v.tutkinnonOsa tuo " +
            "LEFT JOIN tuo.nimi nimi " +
            "LEFT JOIN nimi.teksti teksti " +
            findByPerusteAndNimiWhere,
            countQuery = "SELECT COUNT(v) " +
                    "FROM Peruste p " +
                    "JOIN p.suoritustavat s " +
                    "JOIN s.tutkinnonOsat v " +
                    "JOIN v.tutkinnonOsa tuo " +
                    "LEFT JOIN tuo.nimi nimi " +
                    "LEFT join nimi.teksti teksti " +
                    findByPerusteAndNimiWhere)
    Page<TutkinnonOsaViite> findByPerusteAndNimi(@Param("perusteId") Long perusteId, @Param("nimi") String nimi, @Param("vanhentuneet") boolean vanhentuneet, @Param("kieli") Kieli kieli, Pageable pageable);
}
