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
package fi.vm.sade.eperusteet.domain;

import java.io.Serializable;
import javax.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
/**
 *
 * @author harrik
 */
@Embeddable
@EqualsAndHashCode
public class Diaarinumero implements Serializable {

    public Diaarinumero() {
        
    }

    public Diaarinumero(String diaarinumero) {
        this.diaarinumero = diaarinumero;
    }

    @Getter
    private String diaarinumero;

    @Override
    public String toString() {
        return diaarinumero;
    }


}
