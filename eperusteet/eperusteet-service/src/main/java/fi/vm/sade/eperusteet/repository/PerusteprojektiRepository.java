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

import fi.vm.sade.eperusteet.domain.Diaarinumero;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiKevytDto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author harrik
 */
@Repository
public interface PerusteprojektiRepository extends JpaRepository<Perusteprojekti, Long> {

    List<Perusteprojekti> findByDiaarinumero(Diaarinumero diaarinumero);
    Perusteprojekti findOneByDiaarinumeroAndTila(Diaarinumero diaarinumero, ProjektiTila tila);
    
    Perusteprojekti findOneByPerusteDiaarinumeroAndTila(Diaarinumero diaarinumero, ProjektiTila tila);

    Perusteprojekti findOneByRyhmaOid(String ryhmaOid);

    @Query("SELECT p from Perusteprojekti p WHERE p.tila <> 'POISTETTU' AND p.tila <> 'JULKAISTU' ")
    List<Perusteprojekti> findAllKeskeneraiset();

    @Query("SELECT new fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiKevytDto(" +
            " p.id, " +
            " p.nimi, " +
            " p.peruste.diaarinumero, " +
            " p.diaarinumero, " +
            " p.peruste.koulutustyyppi, " +
            " p.peruste.tyyppi, " +
            " p.tila" +
            ") FROM Perusteprojekti p")
    List<PerusteprojektiKevytDto> findAllKevyt();

}
