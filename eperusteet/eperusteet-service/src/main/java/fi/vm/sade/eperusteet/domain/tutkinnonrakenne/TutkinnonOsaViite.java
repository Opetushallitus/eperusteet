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
package fi.vm.sade.eperusteet.domain.tutkinnonrakenne;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.tekstihaku.TekstihakuCollection;
import fi.vm.sade.eperusteet.domain.tekstihaku.TekstihakuCtx;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.dto.Metalink;
import fi.vm.sade.eperusteet.dto.util.EntityReference;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;


/**
 *
 * @author jhyoty
 */
@Entity
@Table(name = "tutkinnonosaviite")
@Audited
public class TutkinnonOsaViite implements ReferenceableEntity, Serializable, Linkable, Tekstihaettava {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private Suoritustapa suoritustapa;

    @Getter
    @Setter
    @Column(precision = 10, scale = 2)
    private BigDecimal laajuus;

    @Getter
    @Setter
    @Column(name = "laajuus_maksimi", precision = 10, scale = 2)
    private BigDecimal laajuusMaksimi;

    @Getter
    @Setter
    private Integer jarjestys;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @NotNull
    private TutkinnonOsa tutkinnonOsa;

    @Column
    @Getter
    @Setter
    @Temporal(TemporalType.TIMESTAMP)
    private Date muokattu;

    @Override
    public EntityReference getReference() {
        return new EntityReference(id);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.suoritustapa);
        hash = 97 * hash + Objects.hashCode(this.tutkinnonOsa);
        return hash;
    }

    @Override
    public Metalink getMetalink() {
        try {
            Set<Peruste> perusteet = this.getSuoritustapa().getPerusteet();
            if (perusteet.size() == 1) {
                return Metalink.fromTutkinnonOsaViite(
                        perusteet.iterator().next().getId(),
                        this.getSuoritustapa().getSuoritustapakoodi(),
                        getId(),
                        tutkinnonOsa.getId());
            }
            else {
                return null;
            }
        }
        catch (NullPointerException ex) {
            return null;
        }
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof TutkinnonOsaViite) {
            final TutkinnonOsaViite other = (TutkinnonOsaViite) that;
            if (!Objects.equals(this.suoritustapa, other.suoritustapa)) {
                return false;
            }
            if (!Objects.equals(this.tutkinnonOsa, other.tutkinnonOsa)) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void getTekstihaku(TekstihakuCollection haku) {
        getTutkinnonOsa().traverse(haku);
    }

    @Override
    public TekstihakuCtx partialContext() {
        return TekstihakuCtx.builder()
                .tov(this)
                .build();
    }
}
