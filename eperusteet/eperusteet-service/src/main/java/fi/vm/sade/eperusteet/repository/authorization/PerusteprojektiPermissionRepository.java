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
import fi.vm.sade.eperusteet.domain.permissions.PerusteenosanProjekti;
import fi.vm.sade.eperusteet.service.util.Pair;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Lisäkyselyt oikeuksien tarkistelua varten.
 * @author jhyoty
 */
@Repository
public interface PerusteprojektiPermissionRepository extends JpaRepository<Perusteprojekti, Long> {

    @Query("SELECT DISTINCT NEW fi.vm.sade.eperusteet.service.util.Pair(pp.ryhmaOid, pp.tila) FROM Perusteprojekti pp WHERE pp.peruste.id = ?1")
    List<Pair<String, ProjektiTila>> findByPeruste(Long perusteId);

    @Query("SELECT DISTINCT NEW fi.vm.sade.eperusteet.service.util.Pair(pp.ryhmaOid, pp.tila) FROM Perusteprojekti pp WHERE pp.id = ?1")
    List<Pair<String, ProjektiTila>> findById(Long perusteProjektiId);

    @Query("SELECT DISTINCT pp FROM PerusteenosanProjekti pp WHERE pp.id = ?1")
    Set<PerusteenosanProjekti> findAllByPerusteenosa(Long perusteenOsaId);

    /**
     * Etsii perusteprojektit joihin annettu perusteen osa kuuluu ja palauttaa niiden tilan.
     * Huom!. Tarkastelee vain perusteen osia jotka ovat tilassa LUONNOS.
     *
     * Varsinainen kysely on monimutkaisuudestaan johtuen piilotettu näkymän "PerusteenosanProjekti" taakse.
     *
     */
    @Query("SELECT DISTINCT NEW fi.vm.sade.eperusteet.service.util.Pair(pp.ryhmaOid, pp.tila) FROM PerusteenosanProjekti pp WHERE pp.id = ?1")
    List<Pair<String,ProjektiTila>> findTilaByPerusteenOsaId(Long perusteenOsaId);

    @Query("SELECT DISTINCT NEW fi.vm.sade.eperusteet.service.util.Pair(pp.ryhmaOid, pp.esikatseltavissa) FROM Perusteprojekti pp WHERE pp.peruste.id = ?1")
    List<Pair<String, Boolean>> findEsikatseltavissaByPeruste(Long perusteId);

    @Query("SELECT DISTINCT NEW fi.vm.sade.eperusteet.service.util.Pair(pp.ryhmaOid, pp.esikatseltavissa) FROM Perusteprojekti pp WHERE pp.id = ?1")
    List<Pair<String, Boolean>> findEsikatseltavissaById(Long perusteProjektiId);

    @Query("SELECT DISTINCT NEW fi.vm.sade.eperusteet.service.util.Pair(pp.ryhmaOid, pp.esikatseltavissa) FROM PerusteenosanProjekti pp WHERE pp.id = ?1")
    List<Pair<String, Boolean>> findEsikatseltavissaByPerusteenOsaId(Long perusteenOsaId);
}
