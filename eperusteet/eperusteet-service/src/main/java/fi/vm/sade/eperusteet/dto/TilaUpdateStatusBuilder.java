package fi.vm.sade.eperusteet.dto;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.service.util.PerusteenRakenne.Validointi;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

public class TilaUpdateStatusBuilder {
    protected TilaUpdateStatus status;

    protected TilaUpdateStatusBuilder() {
    }

    public TilaUpdateStatusBuilder(TilaUpdateStatus status) {
        this.status = status;
    }

    public TilaUpdateStatusBuilder addStatus(String viesti) {
        status.addStatus(viesti, null, null, (List<LokalisoituTekstiDto>) null);
        return this;
    }

    public TilaUpdateStatusBuilder addStatus(String viesti, Set<Kieli> kielet) {
        status.addStatus(viesti, null, null, null, kielet);
        return this;
    }

    public TilaUpdateStatusBuilder addStatus(String viesti, Suoritustapakoodi suoritustapa) {
        status.addStatus(viesti, suoritustapa, null, (List<LokalisoituTekstiDto>) null);
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

    public TilaUpdateStatusBuilder addStatus(String viesti, Suoritustapakoodi suoritustapa, List<LokalisoituTekstiDto> nimet, ValidointiKategoria validointiKategoria) {
        status.addStatus(viesti, suoritustapa, null, nimet, validointiKategoria);
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

    public TilaUpdateStatusBuilder addStatus(String viesti, List<LokalisoituTekstiDto> nimet, ValidointiKategoria validointiKategoria) {
        status.addStatus(viesti, null, null, nimet, null, validointiKategoria);
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
