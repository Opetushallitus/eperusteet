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

package fi.vm.sade.eperusteet.dto.peruste;

import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.ProjektiTila;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author nkala
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerusteprojektiQueryDto {
    private int sivu = 0;
    private int sivukoko = 25;
    private String nimi;
    private List<PerusteTyyppi> tyyppi = new ArrayList<>();
    private Set<ProjektiTila> tila;
    private Set<String> koulutustyyppi;
    private String jarjestysTapa;
    private Boolean jarjestysOrder;
    private Set<Long> perusteet;
    private boolean tuleva;
    private boolean voimassaolo;
    private boolean siirtyma;
    private boolean poistunut;

    public void setTila(ProjektiTila tila) {
        this.tila = new HashSet<>();
        this.tila.add(tila);
    }

    public void setTila(Set<ProjektiTila> tila) {
        this.tila = tila;
    }
}
