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

package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiKevytDto;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author harrik
 */
@Repository
public interface PerusteprojektiRepository extends JpaRepository<Perusteprojekti, Long>, PerusteprojektiRepositoryCustom {

    List<Perusteprojekti> findByDiaarinumero(Diaarinumero diaarinumero);

    Perusteprojekti findOneByDiaarinumeroAndTila(Diaarinumero diaarinumero, ProjektiTila tila);

    Perusteprojekti findOneByPerusteDiaarinumeroAndTila(Diaarinumero diaarinumero, ProjektiTila tila);

    Perusteprojekti findOneByRyhmaOid(String ryhmaOid);

    Perusteprojekti findOneByPeruste(Peruste peruste);

    @Query("SELECT p from Perusteprojekti p" +
            " WHERE p.peruste.tyyppi = 'NORMAALI' AND p.tila = 'JULKAISTU'" +
            "   AND p.peruste NOT IN (SELECT peruste FROM ValidointiStatus)")
    Set<Perusteprojekti> findAllValidoimattomatUudet();

    @Query("SELECT p from Perusteprojekti p, ValidointiStatus vs" +
            " WHERE p.peruste.tyyppi = 'NORMAALI' AND p.tila = 'JULKAISTU'" +
            " AND vs.peruste = p.peruste AND p.peruste.globalVersion.aikaleima > vs.lastCheck")
    Set<Perusteprojekti> findAllValidoimattomat();

    @Query("SELECT p from Perusteprojekti p" +
            " WHERE p.peruste.tyyppi = 'NORMAALI' AND p.tila = 'JULKAISTU'" +
            "   AND p.peruste NOT IN (SELECT peruste FROM KoulutuskoodiStatus)")
    Set<Perusteprojekti> findAllKoodiValidoimattomatUudet();

    @Query("SELECT p from Perusteprojekti p, KoulutuskoodiStatus kks" +
            " WHERE p.peruste.tyyppi = 'NORMAALI' AND p.tila = 'JULKAISTU'" +
            " AND kks.peruste = p.peruste AND p.peruste.globalVersion.aikaleima > kks.lastCheck")
    Set<Perusteprojekti> findAllKoodiValidoimattomat();

    @Query("SELECT p from Perusteprojekti p" +
            " WHERE p.tila = 'JULKAISTU'" +
            "   AND p.peruste NOT IN (SELECT peruste FROM MaarayskirjeStatus)")
    Set<Perusteprojekti> findAllMaarayskirjeetUudet();

    @Query("SELECT p from Perusteprojekti p, MaarayskirjeStatus mks" +
            " WHERE p.tila = 'JULKAISTU'" +
            " AND mks.peruste = p.peruste AND p.peruste.globalVersion.aikaleima > mks.lastCheck")
    Set<Perusteprojekti> findAllMaarayskirjeet();

    List<Perusteprojekti> findAllByTilaAndPerusteTyyppi(ProjektiTila tila, PerusteTyyppi tyyppi);

    @Query("SELECT p from Perusteprojekti p WHERE p.tila <> 'POISTETTU' AND p.tila <> 'JULKAISTU' AND (p.luoja = ?1 OR p.ryhmaOid IN (?2)) AND p.peruste.tyyppi != 'Opas'")
    List<Perusteprojekti> findOmatPerusteprojektit(String userOid, Set<String> orgs);

    @Query("SELECT new fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiKevytDto(" +
            " p.id, " +
            " p.nimi, " +
            " p.peruste.diaarinumero, " +
            " p.diaarinumero, " +
            " p.peruste.koulutustyyppi, " +
            " p.peruste.tyyppi, " +
            " p.tila" +
            ") FROM Perusteprojekti p WHERE p.peruste.tyyppi != 'Opas'")
    List<PerusteprojektiKevytDto> findAllKevyt();
}
