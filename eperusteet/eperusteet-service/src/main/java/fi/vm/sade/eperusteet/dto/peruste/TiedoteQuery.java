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

import fi.vm.sade.eperusteet.domain.Kieli;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TiedoteQuery {
    private int sivu = 0;
    private int sivukoko = 25;
    private List<Kieli> kieli;
    private String nimi;
    private Long perusteId;
    private Boolean perusteeton; // Jos true, palauttaa vain ne tiedotteet, jotka eivät liity mihinkään perusteeseen
    private Boolean julkinen; // Jos null, haetaan julkiset ja sisäiset
    private Boolean yleinen; // Jos halutaan esittää mm. etusivulla
    private List<String> tiedoteJulkaisuPaikka;
    private List<String> koulutusTyyppi;
    private List<Long> perusteIds;
    private String jarjestys;
    private Boolean jarjestysNouseva;
    private Boolean koulutustyypiton;
}
