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

package fi.vm.sade.eperusteet.dto.perusteprojekti;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author harrik
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown=true)
public class PerusteprojektiLuontiDto extends PerusteprojektiDto {
    private String koulutustyyppi;
    private LaajuusYksikko laajuusYksikko;
    private Long perusteId;
    private ProjektiTila tila;
    private PerusteTyyppi tyyppi;
    private String ryhmaOid;

    public PerusteprojektiLuontiDto(String koulutustyyppi, LaajuusYksikko laajuusYksikko, Long perusteId, ProjektiTila tila, PerusteTyyppi tyyppi, String ryhmaOid) {
        this.koulutustyyppi = koulutustyyppi;
        this.laajuusYksikko = laajuusYksikko;
        this.perusteId = perusteId;
        this.tila = tila;
        this.tyyppi = tyyppi;
        this.ryhmaOid = ryhmaOid;
    }

    public PerusteprojektiLuontiDto() {
    }
}
