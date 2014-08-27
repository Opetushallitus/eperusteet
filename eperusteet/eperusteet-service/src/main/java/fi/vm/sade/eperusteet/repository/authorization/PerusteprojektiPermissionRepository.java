/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.repository.authorization;

import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Lisäkyselyt oikeuksien tarkistelua varten.
 * @author jhyoty
 */
@Repository
public interface PerusteprojektiPermissionRepository extends JpaRepository<Perusteprojekti, Long> {

    @Query("SELECT pp FROM Perusteprojekti pp WHERE pp.peruste.id = ?1")
    Perusteprojekti findByPeruste(Long id);

    /**
     * Etsii perusteprojektit joihin annettu perusteen osa kuuluu ja palauttaa niiden tilan.
     * Huom!. Tarkastelee vain perusteen osia jotka ovat tilassa LUONNOS.
     *
     * Varsinainen kysely on monimutkaisuudestaan johtuen piilotettu näkymän "PerusteenosanProjekti" taakse.
     *
     * @param perusteenOsaId
     * @return
     */
    @Query("SELECT pp.tila FROM PerusteenosanProjekti pp WHERE pp.id = ?1")
    List<ProjektiTila> findTilaByPerusteenOsaId(Long perusteenOsaId);


}
