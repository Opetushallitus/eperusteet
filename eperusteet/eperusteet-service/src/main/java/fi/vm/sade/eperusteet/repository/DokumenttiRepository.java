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

import fi.vm.sade.eperusteet.domain.Dokumentti;
import fi.vm.sade.eperusteet.domain.DokumenttiTila;
import fi.vm.sade.eperusteet.domain.GeneratorVersion;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author jussi
 */
@Repository
public interface DokumenttiRepository extends JpaRepository<Dokumentti, Long> {
    Dokumentti findById(Long id);
    List<Dokumentti> findByPerusteIdAndKieliAndTilaAndSuoritustapakoodiAndGeneratorVersion(
            Long perusteId,
            Kieli kieli,
            DokumenttiTila tila,
            Suoritustapakoodi suoritustapakoodi,
            GeneratorVersion version,
            Sort sort
    );
    List<Dokumentti> findByPerusteIdAndKieliAndTilaAndGeneratorVersion(
            Long perusteId,
            Kieli kieli,
            DokumenttiTila tila,
            GeneratorVersion version,
            Sort sort
    );
}
