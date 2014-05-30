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

import fi.vm.sade.eperusteet.domain.Kommentti;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author nkala
 */
@Repository
public interface KommenttiRepository extends JpaRepository<Kommentti, Long> {
    @Query("SELECT k FROM Kommentti k WHERE k.ylinId = ?1")
    List<Kommentti> findAllByYlin(Long ylinId);

    @Query("SELECT k FROM Kommentti k WHERE k.parentId = ?1")
    List<Kommentti> findAllByParent(Long parentId);

    @Query("SELECT k FROM Kommentti k WHERE k.perusteprojektiId = ?1")
    List<Kommentti> findAllByPerusteprojekti(Long perusteprojektiId);
}
