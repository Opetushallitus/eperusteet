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

package fi.vm.sade.eperusteet.dto;
import fi.vm.sade.eperusteet.domain.Kieli;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author nkala
 */
public class PerusteQuery {
    @Getter @Setter private int sivu = 0;
    @Getter @Setter private int sivukoko= 25;
    @Getter @Setter private boolean siirtyma = false;
    @Getter @Setter private String nimi;
    @Getter @Setter private List<String> ala;
    @Getter @Setter private List<String> tyyppi;
    @Getter @Setter private String kieli = "fi";
    @Getter @Setter private List<String> opintoala;
}
