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

package fi.vm.sade.eperusteet.dto;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.service.util.PerusteenRakenne.Validointi;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * User: tommiratamaa
 * Date: 13.11.2015
 * Time: 14.55
 */
public class TilaUpdateStatusBuilder {
    protected TilaUpdateStatus status;

    protected TilaUpdateStatusBuilder() {
    }

    public TilaUpdateStatusBuilder(TilaUpdateStatus status) {
        this.status = status;
    }

    public TilaUpdateStatusBuilder addStatus(String viesti) {
        status.addStatus(viesti, null, null, null);
        return this;
    }

    public TilaUpdateStatusBuilder addStatus(String viesti, Set<Kieli> kielet) {
        status.addStatus(viesti, null, null, null, kielet);
        return this;
    }

    public TilaUpdateStatusBuilder addStatus(String viesti, Suoritustapakoodi suoritustapa) {
        status.addStatus(viesti, suoritustapa, null, null);
        return this;
    }

    public TilaUpdateStatusBuilder addStatus(String viesti, Suoritustapakoodi suoritustapa, LokalisoituTekstiDto nimi) {
        status.addStatus(viesti, suoritustapa, null, Collections.singletonList(nimi));
        return this;
    }

    public TilaUpdateStatusBuilder addStatus(String viesti, Suoritustapakoodi suoritustapa, List<LokalisoituTekstiDto> nimet) {
        status.addStatus(viesti, suoritustapa, null, nimet);
        return this;
    }

    public TilaUpdateStatusBuilder addStatus(String viesti, Suoritustapakoodi suoritustapa, Validointi validointi) {
        status.addStatus(viesti, suoritustapa, validointi, null);
        return this;
    }

    public TilaUpdateStatusBuilder addStatus(String viesti, Suoritustapakoodi suoritustapa, Validointi validointi, List<LokalisoituTekstiDto> nimet, ValidointiKategoria validointiKategoria) {
        status.addStatus(viesti, suoritustapa, validointi, null, null, validointiKategoria);
        return this;
    }

    public TilaUpdateStatusBuilder addErrorStatus(String viesti, Suoritustapakoodi suoritustapa, LokalisoituTekstiDto... dto) {
        status.addStatus(viesti, suoritustapa, null, asList(dto), null, null, ValidointiStatusType.VIRHE);
        status.setVaihtoOk(false);
        return this;
    }

    public TilaUpdateStatusBuilder addStatus(String viesti, ValidointiKategoria validointiKategoria) {
        status.addStatus(viesti, null, null, null, null, validointiKategoria);
        return this;
    }

    public TilaUpdateStatusBuilder addStatus(String viesti, Set<Kieli> kielet, ValidointiStatusType validointiStatusType) {
        status.addStatus(viesti, null, null, null, kielet, null, validointiStatusType);
        return this;
    }

    public TilaUpdateStatusBuilder addStatus(String viesti, ValidointiStatusType validointiStatusType) {
        status.addStatus(viesti, null, null, null, null, null, validointiStatusType);
        return this;
    }

    public TilaUpdateStatus build() {
        return status;
    }
}
