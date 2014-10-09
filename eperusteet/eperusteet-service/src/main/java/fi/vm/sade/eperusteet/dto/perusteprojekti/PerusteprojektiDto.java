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
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.dto.util.EntityReference;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author harrik
 */
@Getter
@Setter
@JsonIgnoreProperties
public class PerusteprojektiDto implements Serializable {
    private Long id;
    private String nimi;
    private EntityReference peruste;
    private String diaarinumero;
    private Date paatosPvm;
    private Date toimikausiAlku;
    private Date toimikausiLoppu;
    private String tehtavaluokka;
    private String tehtava;
    private String yhteistyotaho;
    private ProjektiTila tila;
    private String ryhmaOid;

    public PerusteprojektiDto(String nimi, EntityReference peruste, String diaarinumero, Date paatosPvm, Date toimikausiAlku, Date toimikausiLoppu, String tehtavaluokka, String tehtava, String yhteistyotaho, ProjektiTila tila, String ryhmaOid) {
        this.nimi = nimi;
        this.peruste = peruste;
        this.diaarinumero = diaarinumero;
        this.paatosPvm = paatosPvm;
        this.toimikausiAlku = toimikausiAlku;
        this.toimikausiLoppu = toimikausiLoppu;
        this.tehtavaluokka = tehtavaluokka;
        this.tehtava = tehtava;
        this.yhteistyotaho = yhteistyotaho;
        this.tila = tila;
        this.ryhmaOid = ryhmaOid;
    }

    public PerusteprojektiDto() {
    }
}
