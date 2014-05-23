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

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author bbl
 */
@Getter
@Setter
public class Henkilo {
    private String henkiloId;
    private String nimi;
    private String puhelinnumero;
    private String email;
    private Rooli rooli;

    public Henkilo() {
    }

    public Henkilo(String henkiloId, String nimi, String puhelinnumero, String email, Rooli rooli) {
        this.henkiloId = henkiloId;
        this.nimi = nimi;
        this.puhelinnumero = puhelinnumero;
        this.email = email;
        this.rooli = rooli;
    }
}
