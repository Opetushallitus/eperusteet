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
import fi.vm.sade.eperusteet.domain.validation.ValidHtml.WhitelistType;
import java.io.Serializable;
import java.util.ArrayList;
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
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 *
 * @author teele1
 */
@Entity
@Table(name = "arviointi")
@Audited
public class Arviointi implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    private Long id;

    @ValidHtml(whitelist = WhitelistType.SIMPLIFIED)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
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

    public void setArvioinninKohdealueet(List<ArvioinninKohdealue> arvioinninKohdealueet) {
        this.arvioinninKohdealueet.clear();
        if (arvioinninKohdealueet != null) {
            this.arvioinninKohdealueet.addAll(arvioinninKohdealueet);
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

}
