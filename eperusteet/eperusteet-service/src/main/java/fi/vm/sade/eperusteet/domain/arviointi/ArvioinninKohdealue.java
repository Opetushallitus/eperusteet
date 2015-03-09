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
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml.WhitelistType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

/**
 *
 * @author teele1
 */
@Entity
@Table(name = "arvioinninkohdealue")
@Audited
public class ArvioinninKohdealue implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ValidHtml(whitelist = WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen otsikko;

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(name = "arvioinninkohdealue_arvioinninkohde",
               joinColumns = @JoinColumn(name = "arvioinninkohdealue_id"),
               inverseJoinColumns = @JoinColumn(name = "arvioinninkohde_id"))
    @OrderColumn
    @BatchSize(size = 10)
    private List<ArvioinninKohde> arvioinninKohteet = new ArrayList<>();

    public ArvioinninKohdealue() {
    }

    public ArvioinninKohdealue(ArvioinninKohdealue other) {
        this.otsikko = other.getOtsikko();
        for (ArvioinninKohde ak : other.getArvioinninKohteet()) {
            this.arvioinninKohteet.add(new ArvioinninKohde(ak));
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TekstiPalanen getOtsikko() {
        return otsikko;
    }

    public void setOtsikko(TekstiPalanen otsikko) {
        this.otsikko = otsikko;
    }

    public List<ArvioinninKohde> getArvioinninKohteet() {
        return arvioinninKohteet;
    }

    public void setArvioinninKohteet(List<ArvioinninKohde> arvioinninKohteet) {
        this.arvioinninKohteet.clear();
        if (arvioinninKohteet != null) {
            this.arvioinninKohteet.addAll(arvioinninKohteet);
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.otsikko);
        hash = 71 * hash + Objects.hashCode(this.arvioinninKohteet);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ArvioinninKohdealue) {
            final ArvioinninKohdealue other = (ArvioinninKohdealue) obj;
            if (!Objects.equals(this.otsikko, other.otsikko)) {
                return false;
            }
            return Objects.equals(this.arvioinninKohteet, other.arvioinninKohteet);
        }
        return false;
    }

    public boolean structureEquals(ArvioinninKohdealue other) {
        boolean result = refXnor(getOtsikko(), other.getOtsikko());
        Iterator<ArvioinninKohde> i = getArvioinninKohteet().iterator();
        Iterator<ArvioinninKohde> j = other.getArvioinninKohteet().iterator();
        while ( result && i.hasNext() && j.hasNext() ) {
            result &= i.next().structureEquals(j.next());
        }
        result &= !i.hasNext();
        result &= !j.hasNext();
        return result;
    }

}
