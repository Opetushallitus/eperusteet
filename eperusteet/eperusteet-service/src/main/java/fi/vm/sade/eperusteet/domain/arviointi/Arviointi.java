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
package fi.vm.sade.eperusteet.domain.arviointi;

import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Osaamistavoite;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml.WhitelistType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

/**
 *
 * @author teele1
 */
@Entity
@Table(name = "arviointi")
@Audited
public class Arviointi implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    private Long id;

    @ValidHtml(whitelist = WhitelistType.SIMPLIFIED)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    private TekstiPalanen lisatiedot;

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(name = "arviointi_arvioinninkohdealue",
               joinColumns = @JoinColumn(name = "arviointi_id"),
               inverseJoinColumns = @JoinColumn(name = "arvioinninkohdealue_id"))
    @OrderColumn
    @Getter
    private List<ArvioinninKohdealue> arvioinninKohdealueet = new ArrayList<>();

    @Getter
    @Setter
    @NotAudited
    @RelatesToPeruste
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "arviointi")
    private Osaamistavoite osaamistavoite;

    @Getter
    @Setter
    @NotAudited
    @RelatesToPeruste
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "arviointi")
    private TutkinnonOsa tutkinnonOsa;

    public Arviointi() {
    }

    public Arviointi(Arviointi other) {
        copyState(other);
    }

    public void setArvioinninKohdealueet(List<ArvioinninKohdealue> alueet) {
        if (this.arvioinninKohdealueet == alueet) {
            return;
        }
        if (alueet != null) {
            ArrayList<ArvioinninKohdealue> tmp = new ArrayList<>(alueet.size());
            for (ArvioinninKohdealue a : alueet) {
                int i = this.arvioinninKohdealueet.indexOf(a);
                if (i >= 0) {
                    tmp.add(this.arvioinninKohdealueet.get(i));
                } else {
                    tmp.add(a);
                }
            }
            this.arvioinninKohdealueet.clear();
            this.arvioinninKohdealueet.addAll(tmp);
        } else {
            this.arvioinninKohdealueet.clear();
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.lisatiedot);
        hash = 67 * hash + Objects.hashCode(this.arvioinninKohdealueet);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Arviointi) {
            final Arviointi other = (Arviointi) obj;
            if (!Objects.equals(this.lisatiedot, other.lisatiedot)) {
                return false;
            }
            return Objects.equals(this.arvioinninKohdealueet, other.arvioinninKohdealueet);
        }
        return false;
    }

    public void mergeState(Arviointi other) {
        if (this != other) {
            this.setLisatiedot(other.getLisatiedot());
            this.setArvioinninKohdealueet(other.getArvioinninKohdealueet());
        }
    }

    public boolean structureEquals(Arviointi other) {
        if (this == other) {
            return true;
        }
        boolean result = refXnor(getLisatiedot(), other.getLisatiedot());
        Iterator<ArvioinninKohdealue> i = getArvioinninKohdealueet().iterator();
        Iterator<ArvioinninKohdealue> j = other.getArvioinninKohdealueet().iterator();
        while (result && i.hasNext() && j.hasNext()) {
            result &= i.next().structureEquals(j.next());
        }
        result &= !i.hasNext();
        result &= !j.hasNext();
        return result;
    }

    private void copyState(Arviointi other) {
        this.setLisatiedot(other.getLisatiedot());
        for (ArvioinninKohdealue aka : other.getArvioinninKohdealueet()) {
            this.arvioinninKohdealueet.add(new ArvioinninKohdealue(aka));
        }
    }

}
