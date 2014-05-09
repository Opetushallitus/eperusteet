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

import java.util.Date;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author jhyoty
 */
@Getter
@Setter
//@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.NAME, property = "tyyppi")
//@JsonSubTypes({@JsonSubTypes.Type(TekstiKappaleDto.class), @JsonSubTypes.Type(TutkinnonOsaDto.class)})
public abstract class PerusteenOsaDto {
    @NotNull
    private Long id;
    private Date luotu;
    private Date muokattu;
    private LokalisoituTekstiDto nimi;
}
