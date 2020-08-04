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

import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.dto.Reference;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.springframework.util.ObjectUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

/**
 *
 * @author jhyoty
 */
@Entity
@Table(name = "tekstikappale")
@Audited
public class TekstiKappale extends PerusteenOsa implements Serializable {

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen teksti;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Koodi osaamisala;

    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(name = "tekstikappale_koodi",
            joinColumns = @JoinColumn(name = "tekstikappale_id"),
            inverseJoinColumns = @JoinColumn(name = "koodi_id"))
    @Column(name = "koodi_id")
    private List<Koodi> koodit;

    @Getter
    @Setter
    @NotNull
    private boolean liite = false;

    public TekstiKappale() {
    }

    public TekstiKappale(TekstiKappale other) {
        super(other);
        copyState(other);
    }

    @Override
    public Reference getReference() {
	return new Reference(getId());
    }

    public TekstiPalanen getTeksti() {
        return teksti;
    }

    public void setTeksti(TekstiPalanen teksti) {
        this.teksti = teksti;
    }

    @Override
    public void mergeState(PerusteenOsa perusteenOsa) {
        super.mergeState(perusteenOsa);
        if (perusteenOsa instanceof TekstiKappale) {
            copyState((TekstiKappale) perusteenOsa);
        }
    }

    @Override
    public TekstiKappale copy() {
        return new TekstiKappale(this);
    }

    @Override
    public boolean structureEquals(PerusteenOsa other) {
        boolean result = false;
        if (other instanceof TekstiKappale) {
            TekstiKappale that = (TekstiKappale)other;
            result = super.structureEquals(that);
            result &= getOsaamisala() == null || Objects.equals(getOsaamisala(), that.getOsaamisala());
            // Sallitaan liitetiedon muutos julkaistulle perusteelle
            // result &= Objects.equals(isLiite(), that.isLiite());
            result &= getTeksti() == null || refXnor(getTeksti(), that.getTeksti());
         }
        return result;
    }

    private void copyState(TekstiKappale other) {
        this.setTeksti(other.getTeksti());
        this.setOsaamisala(other.getOsaamisala());
        List<Koodi> oKoodit = other.getKoodit();
        if (!ObjectUtils.isEmpty(oKoodit)) {
            ArrayList<Koodi> koodit = new ArrayList<>();
            for (Koodi oKoodi : oKoodit) {
                Koodi koodi = new Koodi(oKoodi.getUri(), oKoodi.getKoodisto());
                koodit.add(koodi);
            }
            this.setKoodit(koodit);
        }
    }

}
