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

import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.arviointi.ArvioinninKohde;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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

    private Integer laajuus;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen opetuksenTavoiteOtsikko;

    @OrderColumn
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToMany(cascade = {CascadeType.ALL})
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
            copyState((Opintokokonaisuus) perusteenOsa);
        }
    }

    @Override
    public boolean structureEquals(PerusteenOsa other) {
        return false;
    }

    private void copyState(Opintokokonaisuus other) {
        if (other == null) {
            return;
        }

        setNimi(other.getNimi());
        setNimiKoodi(other.getNimiKoodi());
        setKuvaus(other.getKuvaus());
        setLaajuus((other.getLaajuus()));
        setOpetuksenTavoiteOtsikko((other.getOpetuksenTavoiteOtsikko()));

        this.opetuksenTavoitteet = new ArrayList<>();
        for (Koodi koodi : other.getOpetuksenTavoitteet()) {
            this.opetuksenTavoitteet.add(koodi);
        }

        this.arvioinnit = new ArrayList<>();
        setArvioinnit(other.getArvioinnit());
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.opintokokonaisuus;
    }

    public void addOpetuksenTavoite(Koodi opetuksenTavoite) {
        this.opetuksenTavoitteet.add(opetuksenTavoite);
    }
}
