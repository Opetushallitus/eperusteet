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
package fi.vm.sade.eperusteet.domain.Arviointi;

import fi.vm.sade.eperusteet.domain.OsaamistasonKriteeri;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidArvioinninKohde;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml.WhitelistType;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 *
 * @author teele1
 */
@Entity
@Table(name = "arvioinninkohde")
@ValidArvioinninKohde
@Audited
public class ArvioinninKohde implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @ValidHtml(whitelist = WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen otsikko;

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private ArviointiAsteikko arviointiAsteikko;

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    @Getter
    @BatchSize(size = 10)
    private Set<OsaamistasonKriteeri> osaamistasonKriteerit = new HashSet<>();

    public ArvioinninKohde() {
    }

    public ArvioinninKohde(ArvioinninKohde other) {
        this.otsikko = other.getOtsikko();
        this.arviointiAsteikko = other.getArviointiAsteikko();
        for ( OsaamistasonKriteeri k : other.getOsaamistasonKriteerit() ) {
            this.osaamistasonKriteerit.add(new OsaamistasonKriteeri(k));
        }
    }

    public void setOsaamistasonKriteerit(Set<OsaamistasonKriteeri> osaamistasonKriteerit) {
        this.osaamistasonKriteerit.clear();
        if (osaamistasonKriteerit != null) {
            this.osaamistasonKriteerit.addAll(osaamistasonKriteerit);
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Objects.hashCode(this.otsikko);
        hash = 41 * hash + Objects.hashCode(this.arviointiAsteikko);
        hash = 41 * hash + Objects.hashCode(this.osaamistasonKriteerit);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ArvioinninKohde) {
            final ArvioinninKohde other = (ArvioinninKohde) obj;
            if (!Objects.equals(this.otsikko, other.otsikko)) {
                return false;
            }
            if (!Objects.equals(this.arviointiAsteikko, other.arviointiAsteikko)) {
                return false;
            }
            return Objects.equals(this.osaamistasonKriteerit, other.osaamistasonKriteerit);
        }
        return false;
    }

}
