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

import fi.vm.sade.eperusteet.domain.ProjektiTila;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author nkala
 */
@Getter
@Setter
public class PerusteprojektiQueryDto {
    private int sivu = 0;
    private int sivukoko = 25;
    private String nimi;
    private Set<ProjektiTila> tila;

    public void setTila(ProjektiTila tila) {
        this.tila = new HashSet<>();
        this.tila.add(tila);
    }

    public void setTila(Set<ProjektiTila> tila) {
        this.tila = tila;
    }
}
