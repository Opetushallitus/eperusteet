/*
 *  Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.dto.perusteprojekti;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fi.vm.sade.eperusteet.domain.Diaarinumero;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import fi.vm.sade.eperusteet.dto.peruste.PerusteBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteVersionDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties
public class PerusteprojektiKevytDto implements Serializable  {
    private Long id;
    private String nimi;
    private ProjektiTila tila;
    private String perusteendiaarinumero;
    private String diaarinumero;
    private String koulutustyyppi;
    private KoulutustyyppiToteutus toteutus;
    private PerusteTyyppi tyyppi;
    private Set<String> suoritustavat;
    private Date luotu;
    private PerusteVersionDto globalVersion;
    private PerusteBaseDto peruste;

    public PerusteprojektiKevytDto(Long id, String nimi, Diaarinumero perusteDiaari, Diaarinumero diaarinumero,
                                   String koulutustyyppi, PerusteTyyppi tyyppi, ProjektiTila tila) {
        this.id = id;
        this.nimi = nimi;
        this.tila = tila;
        this.perusteendiaarinumero = (perusteDiaari != null) ? perusteDiaari.toString() : null;
        this.diaarinumero = (diaarinumero != null) ? diaarinumero.toString() : null;
        this.koulutustyyppi = koulutustyyppi;
        this.tyyppi = tyyppi;
    }
}
