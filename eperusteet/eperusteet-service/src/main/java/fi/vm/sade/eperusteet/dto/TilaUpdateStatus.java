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
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiListausDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.service.util.PerusteenRakenne.Validointi;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

/**
 *
 * @author harrik
 */
public class TilaUpdateStatus extends TilaUpdateStatusBuilder {
    @Getter
    @Setter
    private PerusteprojektiListausDto perusteprojekti;

    @Getter
    @Setter
    private Date lastCheck;

    @Getter
    @Setter
    List<Status> infot = new ArrayList<>();

    @Getter
    @Setter
    boolean vaihtoOk = true;

    public TilaUpdateStatus() {
        status = this;
    }

    public void merge(TilaUpdateStatus other) {
        if (other != null) {
            vaihtoOk = vaihtoOk && other.isVaihtoOk();
            infot.addAll(other.infot);
        }
    }

    public static class TilaUpdateStatusBuilderForSuoritustapa {
        private final TilaUpdateStatusBuilder builder;
        @Getter
        private final Suoritustapakoodi suoritustapa;

        public TilaUpdateStatusBuilderForSuoritustapa(TilaUpdateStatusBuilder builder, Suoritustapakoodi suoritustapa) {
            this.builder = builder;
            this.suoritustapa = suoritustapa;
        }

        public TilaUpdateStatusBuilderForSuoritustapa addErrorStatus(String viesti, LokalisoituTekstiDto... tekstit) {
            if (isUsed()) {
                builder.addErrorStatus(viesti, suoritustapa, tekstit);
            }
            return this;
        }

        public TilaUpdateStatusBuilderForSuoritustapa addErrorStatusForAll(String viesti,
                               Supplier<Stream<LokalisoituTekstiDto>> all) {
            if (isUsed()) {
                LokalisoituTekstiDto[] tekstit = all.get().toArray(LokalisoituTekstiDto[]::new);
                if (tekstit.length > 0) {
                    builder.addErrorStatus(viesti, suoritustapa, tekstit);
                }
            }
            return this;
        }

        public TilaUpdateStatusBuilderForSuoritustapa addErrorGiven(String viesti, boolean given) {
            if (given) {
                builder.addErrorStatus(viesti, suoritustapa);
            }
            return this;
        }

        public TilallinenTilaUpdateStatusBuilderForSuoritustapa toTila(ProjektiTila tila) {
            return new TilallinenTilaUpdateStatusBuilderForSuoritustapa(this.builder, this.suoritustapa, tila);
        }

        protected boolean isUsed() {
            return true;
        }
    }

    public static class TilallinenTilaUpdateStatusBuilderForSuoritustapa extends TilaUpdateStatusBuilderForSuoritustapa {
        private final ProjektiTila targetTila;
        private Set<ProjektiTila> tilat = new HashSet<>();

        public TilallinenTilaUpdateStatusBuilderForSuoritustapa(TilaUpdateStatusBuilder builder, Suoritustapakoodi suoritustapa,
                                                      ProjektiTila targetTila) {
            super(builder, suoritustapa);
            this.targetTila = targetTila;
        }

        @Override
        public boolean isUsed() {
            return tilat.isEmpty() || tilat.contains(targetTila);
        }

        public TilallinenTilaUpdateStatusBuilderForSuoritustapa forTilat(ProjektiTila ...tilat) {
            this.tilat = new HashSet<>(asList(tilat));
            return this;
        }

        public TilallinenTilaUpdateStatusBuilderForSuoritustapa check(
                        Consumer<TilallinenTilaUpdateStatusBuilderForSuoritustapa> status) {
            if (isUsed()) {
                status.accept(this);
            }
            return this;
        }

        @Override
        public TilallinenTilaUpdateStatusBuilderForSuoritustapa addErrorStatus(String viesti, LokalisoituTekstiDto... tekstit) {
            super.addErrorStatus(viesti, tekstit);
            return this;
        }
        @Override
        public TilallinenTilaUpdateStatusBuilderForSuoritustapa addErrorStatusForAll(String viesti,
                                                             Supplier<Stream<LokalisoituTekstiDto>> all) {
            super.addErrorStatusForAll(viesti, all);
            return this;
        }
        @Override
        public TilallinenTilaUpdateStatusBuilderForSuoritustapa addErrorGiven(String viesti, boolean given) {
            super.addErrorGiven(viesti, given);
            return this;
        }
    }

    public TilaUpdateStatusBuilderForSuoritustapa forSuoritustapa(Suoritustapakoodi suoritustapa) {
        return new TilaUpdateStatusBuilderForSuoritustapa(this, suoritustapa);
    }

    public void addStatus(String viesti, Suoritustapakoodi suoritustapa, Validointi validointi, List<LokalisoituTekstiDto> nimet) {
        if (infot == null) {
            infot = new ArrayList<>();
        }
        infot.add(new Status(viesti, suoritustapa, validointi, nimet));
    }

    public void addStatus(String viesti, Suoritustapakoodi suoritustapa, Validointi validointi, List<LokalisoituTekstiDto> nimet, Set<Kieli> kielet) {
        if (infot == null) {
            infot = new ArrayList<>();
        }
        infot.add(new Status(viesti, suoritustapa, validointi, nimet, kielet));
    }

    public void addStatus(String viesti, Suoritustapakoodi suoritustapa, Validointi validointi, List<LokalisoituTekstiDto> nimet, Set<Kieli> kielet, ValidointiKategoria validointiKategoria) {
        if (infot == null) {
            infot = new ArrayList<>();
        }
        infot.add(new Status(viesti, suoritustapa, validointi, nimet, kielet, validointiKategoria));
    }

    @Getter
    @Setter
    public static class Status {
        String viesti;
        PerusteprojektiListausDto perusteprojekti;
        Date lastCheck;
        Validointi validointi;
        List<LokalisoituTekstiDto> nimet = new ArrayList<>();
        Suoritustapakoodi suoritustapa;
        Set<Kieli> kielet;
        ValidointiKategoria validointiKategoria = ValidointiKategoria.MAARITTELEMATON;

        public Status() {
        }
        
        public Status(String viesti, Suoritustapakoodi suoritustapa, Validointi validointi, List<LokalisoituTekstiDto> nimet) {
            this.viesti = viesti;
            this.validointi = validointi;
            this.nimet = nimet;
            this.suoritustapa = suoritustapa;
        }

        public Status(String viesti, Suoritustapakoodi suoritustapa, Validointi validointi, List<LokalisoituTekstiDto> nimet, Set<Kieli> kielet) {
            this.viesti = viesti;
            this.validointi = validointi;
            this.nimet = nimet;
            this.suoritustapa = suoritustapa;
            this.kielet = kielet;
        }

        public Status(String viesti, Suoritustapakoodi suoritustapa, Validointi validointi, List<LokalisoituTekstiDto> nimet, Set<Kieli> kielet, ValidointiKategoria validointiKategoria) {
            this.viesti = viesti;
            this.validointi = validointi;
            this.nimet = nimet;
            this.suoritustapa = suoritustapa;
            this.kielet = kielet;
            this.validointiKategoria = validointiKategoria;
        }
    }

}
