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
import fi.vm.sade.eperusteet.dto.EntityReference;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 *
 * @author jhyoty
 */
@Entity
@Table(name = "peruste")
@Audited
public class Peruste extends AbstractAuditedEntity implements Serializable, ReferenceableEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    @Setter
    private Long id;
    
    @ValidHtml(whitelist = WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen nimi;
    
    @Getter
    @Setter
    private String tutkintokoodi;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "peruste_koulutus",
            joinColumns = @JoinColumn(name = "peruste_id"),
            inverseJoinColumns = @JoinColumn(name = "koulutus_id"))
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Set<Koulutus> koulutukset;

    @Temporal(TemporalType.TIMESTAMP)
    @Getter
    @Setter
    private Date paivays;

    @Temporal(TemporalType.TIMESTAMP)
    @Getter
    @Setter
    private Date siirtyma;
    
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapKey(name = "suoritustapakoodi")
    @JoinTable(name = "peruste_suoritustapa",
            joinColumns = @JoinColumn(name = "peruste_id"),
            inverseJoinColumns = @JoinColumn(name = "suoritustapa_id"))
    @Getter
    @Setter
    private Set<Suoritustapa> suoritustavat;

    public Suoritustapa getSuoritustapa(Suoritustapakoodi koodi) {
        for ( Suoritustapa s : suoritustavat ) {
            if ( s.getSuoritustapakoodi() == koodi) {
                return s;
            }
        }
        throw new IllegalArgumentException("Perusteella ei ole pyydettyä suoritustapaa");
    }
    
    @Override
    public EntityReference getReference() {
        return new EntityReference(id);
    }

}