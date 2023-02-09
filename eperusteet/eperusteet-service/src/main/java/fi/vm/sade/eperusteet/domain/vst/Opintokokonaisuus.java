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

package fi.vm.sade.eperusteet.domain.vst;

import fi.vm.sade.eperusteet.domain.KevytTekstiKappale;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.arviointi.ArvioinninKohde;
import fi.vm.sade.eperusteet.domain.digi.OsaamiskokonaisuusKasitteisto;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Ammattitaitovaatimus2019;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.OsaAlue;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidKoodisto;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoUriArvo;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

@Entity
@Table(name = "opintokokonaisuus")
@Audited
@Getter
@Setter
public class Opintokokonaisuus extends PerusteenOsa implements Serializable {
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Koodi nimiKoodi;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    private Integer minimilaajuus;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen opetuksenTavoiteOtsikko;

    @OrderColumn
    @ValidKoodisto(koodisto = KoodistoUriArvo.OPINTOKOKONAISUUSTAVOITTEET)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "opintokokonaisuus_opetuksen_tavoitteet",
            joinColumns = @JoinColumn(name = "peruste_id"),
            inverseJoinColumns = @JoinColumn(name = "opetuksentavoite_id"))
    @Column(name = "opetuksentavoite_id")
    private List<Koodi> opetuksenTavoitteet = new ArrayList<>();

    @OrderColumn
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinTable(name = "opintokokonaisuus_arvioinnit",
            joinColumns = @JoinColumn(name = "peruste_id"),
            inverseJoinColumns = @JoinColumn(name = "arviointi_id"))
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private List<TekstiPalanen> arvioinnit = new ArrayList<>();

    public Opintokokonaisuus() {

    }

    public Opintokokonaisuus(Opintokokonaisuus other) {
        super(other);
        copyState(other);
    }

    public void setOpetuksenTavoitteet(List<Koodi> opetuksenTavoitteet) {
        this.opetuksenTavoitteet.clear();
        if (opetuksenTavoitteet != null) {
            this.opetuksenTavoitteet.addAll(opetuksenTavoitteet);
        }
    }

    @Override
    public Opintokokonaisuus copy() {
        return new Opintokokonaisuus(this);
    }

    @Override
    public Reference getReference() {
        return new Reference(getId());
    }

    @Override
    public void mergeState(PerusteenOsa perusteenOsa) {
        super.mergeState(perusteenOsa);
        if (perusteenOsa instanceof Opintokokonaisuus) {
            Opintokokonaisuus other = (Opintokokonaisuus) perusteenOsa;
            setNimi(other.getNimi());
            setNimiKoodi(other.getNimiKoodi());
            setKuvaus(other.getKuvaus());
            setMinimilaajuus((other.getMinimilaajuus()));
            setOpetuksenTavoiteOtsikko((other.getOpetuksenTavoiteOtsikko()));

            this.opetuksenTavoitteet = new ArrayList<>();
            for (Koodi koodi : other.getOpetuksenTavoitteet()) {
                this.opetuksenTavoitteet.add(koodi);
            }

            this.arvioinnit = new ArrayList<>();
            setArvioinnit(other.getArvioinnit());
        }
    }

    @Override
    public boolean structureEquals(PerusteenOsa updated) {
        boolean result = false;
        if (updated instanceof Opintokokonaisuus) {
            Opintokokonaisuus that = (Opintokokonaisuus) updated;
            result = super.structureEquals(that);
            result &= getKuvaus() == null || refXnor(getKuvaus(), that.getKuvaus());
            result &= Objects.equals(getNimiKoodi(), that.getNimiKoodi());
            result &= Objects.equals(getMinimilaajuus(), that.getMinimilaajuus());
            result &= Objects.equals(getMinimilaajuus(), that.getMinimilaajuus());
            result &= refXnor(getOpetuksenTavoiteOtsikko(), that.getOpetuksenTavoiteOtsikko());
            result &= refXnor(getOpetuksenTavoitteet(), that.getOpetuksenTavoitteet());
            result &= refXnor(getArvioinnit(), that.getArvioinnit());

            if (result && getOpetuksenTavoitteet() != null) {
                Iterator<Koodi> i = getOpetuksenTavoitteet().iterator();
                Iterator<Koodi> j = that.getOpetuksenTavoitteet().iterator();
                while (result && i.hasNext() && j.hasNext()) {
                    result &= Objects.equals(i.next(), j.next());
                }
                result &= !i.hasNext();
                result &= !j.hasNext();
            }

            if (result && getArvioinnit() != null) {
                Iterator<TekstiPalanen> i = getArvioinnit().iterator();
                Iterator<TekstiPalanen> j = that.getArvioinnit().iterator();
                while (result && i.hasNext() && j.hasNext()) {
                    result &= Objects.equals(i.next(), j.next());
                }
                result &= !i.hasNext();
                result &= !j.hasNext();
            }

        }
        return result;
    }

    private void copyState(Opintokokonaisuus other) {
        if (other == null) {
            return;
        }

        this.kuvaus = other.kuvaus;
        this.nimiKoodi = other.getNimiKoodi();
        this.minimilaajuus = other.getMinimilaajuus();
        this.opetuksenTavoiteOtsikko = other.getOpetuksenTavoiteOtsikko();
        this.opetuksenTavoitteet = other.getOpetuksenTavoitteet().stream().map(k -> new Koodi(k.getUri())).collect(Collectors.toList());
        this.arvioinnit = other.getArvioinnit().stream().map(a -> TekstiPalanen.of(a.getTeksti())).collect(Collectors.toList());
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.opintokokonaisuus;
    }

    public void addOpetuksenTavoite(Koodi opetuksenTavoite) {
        this.opetuksenTavoitteet.add(opetuksenTavoite);
    }
}
