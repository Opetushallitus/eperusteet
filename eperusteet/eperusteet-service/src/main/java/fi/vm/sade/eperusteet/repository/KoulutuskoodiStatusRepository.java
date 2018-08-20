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

import fi.vm.sade.eperusteet.domain.KoulutuskoodiStatus;
import fi.vm.sade.eperusteet.domain.Peruste;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface KoulutuskoodiStatusRepository extends JpaRepository<KoulutuskoodiStatus, Long> {
    KoulutuskoodiStatus findOneByPeruste(Peruste peruste);

    @Query("SELECT vs FROM KoulutuskoodiStatus vs WHERE vs.kooditOk = FALSE OR vs.kooditOk IS NULL")
    Page<KoulutuskoodiStatus> findOngelmalliset(Pageable pageable);
}
