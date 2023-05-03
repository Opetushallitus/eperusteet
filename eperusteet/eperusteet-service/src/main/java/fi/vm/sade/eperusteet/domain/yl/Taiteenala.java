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

package fi.vm.sade.eperusteet.domain.yl;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fi.vm.sade.eperusteet.domain.KevytTekstiKappale;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nkala
 */
@Entity
@Table(name = "taiteenala")
@JsonTypeName("taiteenala")
@Audited
@Getter
@Setter
public class Taiteenala extends PerusteenOsa implements Serializable {
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Koodi koodi;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen teksti;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private KevytTekstiKappale kasvatus;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private KevytTekstiKappale yhteisetOpinnot;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private KevytTekstiKappale teemaopinnot;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private KevytTekstiKappale aikuistenOpetus;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private KevytTekstiKappale tyotavatOpetuksessa;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private KevytTekstiKappale oppimisenArviointiOpetuksessa;

    @Getter
    @Setter
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinTable(name = "taiteenala_vapaateksti",
            joinColumns = @JoinColumn(name = "taiteenala_id"),
            inverseJoinColumns = @JoinColumn(name = "kevyttekstikappale_id"))
    @OrderColumn(name = "kevyttekstikappaleet_order")
    private List<KevytTekstiKappale> vapaatTekstit;

    public Taiteenala() {

    }

    public Taiteenala(Taiteenala other) {
        copyState(other);
    }

    @Override
    public Taiteenala copy() {
        return new Taiteenala(this);
    }

    @Override
    public Reference getReference() {
        return new Reference(getId());
    }

    @Override
    public void mergeState(PerusteenOsa perusteenOsa) {
        super.mergeState(perusteenOsa);
        if (perusteenOsa instanceof Taiteenala) {
            copyState((Taiteenala) perusteenOsa);
        }
    }

    @Override
    public boolean structureEquals(PerusteenOsa other) {
        return false;
    }

    private void copyState(Taiteenala other) {
        if (other == null) {
            return;
        }

        setKoodi(other.getKoodi());
        setTeksti(other.getTeksti());
        setAikuistenOpetus(KevytTekstiKappale.getCopy(other.getAikuistenOpetus()));
        setYhteisetOpinnot(KevytTekstiKappale.getCopy(other.getYhteisetOpinnot()));
        setTeemaopinnot(KevytTekstiKappale.getCopy(other.getTeemaopinnot()));
        setKasvatus(KevytTekstiKappale.getCopy(other.getKasvatus()));
        setTyotavatOpetuksessa(KevytTekstiKappale.getCopy(other.getTyotavatOpetuksessa()));
        setOppimisenArviointiOpetuksessa(KevytTekstiKappale.getCopy(other.getOppimisenArviointiOpetuksessa()));

        if (other.getVapaatTekstit() != null) {
            this.vapaatTekstit = new ArrayList<>();
            for (KevytTekstiKappale vapaaTeksti : other.getVapaatTekstit()) {
                this.vapaatTekstit.add(new KevytTekstiKappale(vapaaTeksti));
            }
        }
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.taiteenala;
    }
}
