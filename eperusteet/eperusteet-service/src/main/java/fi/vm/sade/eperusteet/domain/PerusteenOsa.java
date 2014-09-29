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

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml.WhitelistType;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author jhyoty
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Audited
@Table(name = "perusteenosa")
public abstract class PerusteenOsa
    extends AbstractAuditedEntity
    implements Serializable, Mergeable<PerusteenOsa>, WithPerusteTila, ReferenceableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ValidHtml(whitelist = WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen nimi;

    @Getter
    @Setter
    @Enumerated(value = EnumType.STRING)
    @NotNull
    private PerusteTila tila = PerusteTila.LUONNOS;

    @Getter
    @Setter
    @Enumerated(value = EnumType.STRING)
    private PerusteenOsaTunniste tunniste;

    public PerusteenOsa() {
        //JPA
    }

    //copy constuctor
    public PerusteenOsa(PerusteenOsa other) {
        copyState(other);
    }


    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TekstiPalanen getNimi() {
        return nimi;
    }

    public void setNimi(TekstiPalanen nimi) {
        this.nimi = nimi;
    }

    @Override
    public void mergeState(PerusteenOsa updated) {
        if (this.getClass().isAssignableFrom(updated.getClass()) && getId() == null || !getId().equals(updated.getId())) {
            throw new IllegalArgumentException("Vain kahden saman entiteetin tilan voi yhdistää");
        }
        copyState(updated);
    }

    public abstract PerusteenOsa copy();

    private void copyState(PerusteenOsa other) {
        this.nimi = other.getNimi();
    }

}
