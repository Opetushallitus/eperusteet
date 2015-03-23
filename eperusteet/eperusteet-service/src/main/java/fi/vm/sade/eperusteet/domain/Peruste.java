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
import fi.vm.sade.eperusteet.domain.yl.EsiopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.PerusopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.dto.util.EntityReference;
import java.io.Serializable;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
public class Peruste extends AbstractAuditedEntity implements Serializable, ReferenceableEntity, WithPerusteTila {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @ValidHtml(whitelist = WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @NotNull(groups = Valmis.class)
    private TekstiPalanen nimi;

    @ValidHtml(whitelist = WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @NotNull(groups = Valmis.class)
    private TekstiPalanen kuvaus;

    @Getter
    @Setter
    @NotNull(groups = Valmis.class)
    private Diaarinumero diaarinumero;

    @Getter
    @Setter
    @NotNull(groups = Valmis.class)
    private String koulutustyyppi;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "peruste_koulutus",
               joinColumns = @JoinColumn(name = "peruste_id"),
               inverseJoinColumns = @JoinColumn(name = "koulutus_id"))
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Set<Koulutus> koulutukset;

    @ElementCollection
    @Getter
    @Setter
    @CollectionTable(name = "korvattavat_diaarinumerot")
    private Set<Diaarinumero> korvattavatDiaarinumerot;

    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "peruste_osaamisala",
               joinColumns = @JoinColumn(name = "peruste_id"),
               inverseJoinColumns = @JoinColumn(name = "osaamisala_id"))
    @Column(name = "osaamisala_id")
    private Set<Koodi> osaamisalat = new HashSet<>();
    
    @Temporal(TemporalType.TIMESTAMP)
    @Getter
    @Setter
    @Column(name = "voimassaolo_alkaa")
    @NotNull(groups = Valmis.class)
    private Date voimassaoloAlkaa;

    @Temporal(TemporalType.TIMESTAMP)
    @Getter
    @Setter
    @Column(name = "voimassaolo_loppuu")
    private Date voimassaoloLoppuu;

    @Temporal(TemporalType.TIMESTAMP)
    @Getter
    @Setter
    @Column(name = "siirtyma_paattyy")
    private Date siirtymaPaattyy;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapKey(name = "suoritustapakoodi")
    @JoinTable(name = "peruste_suoritustapa",
               joinColumns = @JoinColumn(name = "peruste_id"),
               inverseJoinColumns = @JoinColumn(name = "suoritustapa_id"))
    @Getter
    @Setter
    private Set<Suoritustapa> suoritustavat;

    @Getter
    @OneToOne(mappedBy = "peruste", optional = true, fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private PerusopetuksenPerusteenSisalto perusopetuksenPerusteenSisalto;

    @Getter
    @OneToOne(mappedBy = "peruste", optional = true, fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private EsiopetuksenPerusteenSisalto esiopetuksenPerusteenSisalto;

    @Getter
    @Enumerated(EnumType.STRING)
    @NotNull
    private PerusteTila tila = PerusteTila.LUONNOS;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @NotNull
    private PerusteTyyppi tyyppi = PerusteTyyppi.NORMAALI;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    @CollectionTable(name = "peruste_kieli")
    @Column(name = "kieli")
    /**
     * Kielet jolla peruste tarjotaan. Oletuksena suomi ja ruotsi.
     */
    @Size(min = 1, groups = Valmis.class)
    private Set<Kieli> kielet = EnumSet.of(Kieli.FI, Kieli.SV);

    public Suoritustapa getSuoritustapa(Suoritustapakoodi koodi) {
        for (Suoritustapa s : suoritustavat) {
            if (s.getSuoritustapakoodi() == koodi) {
                return s;
            }
        }
        throw new IllegalArgumentException("Perusteella ei ole pyydettyä suoritustapaa");
    }

    @Override
    public void asetaTila(PerusteTila tila) {
        this.tila = tila;
    }

    @Override
    public EntityReference getReference() {
        return new EntityReference(id);
    }

    /*
    Palauttaa suoritustavan mukaisen sisällön.
    */
    public PerusteenOsaViite getSisalto(Suoritustapakoodi suoritustapakoodi) {
        PerusteenOsaViite viite = null;
        if (suoritustapakoodi.equals(Suoritustapakoodi.OPS) || suoritustapakoodi.equals(Suoritustapakoodi.NAYTTO)) {
            for(Suoritustapa suoritustapa : this.getSuoritustavat()) {
                if (suoritustapa.getSuoritustapakoodi().equals(suoritustapakoodi)) {
                    viite = suoritustapa.getSisalto();
                }
            }
        } else if (suoritustapakoodi.equals(Suoritustapakoodi.ESIOPETUS) || suoritustapakoodi.equals(Suoritustapakoodi.LISAOPETUS)) {
            viite = this.getEsiopetuksenPerusteenSisalto().getSisalto();
        } else if (suoritustapakoodi.equals(Suoritustapakoodi.PERUSOPETUS)) {
            viite = this.getPerusopetuksenPerusteenSisalto().getSisalto();
        }
        return viite;
    }

    public void setPerusopetuksenPerusteenSisalto(PerusopetuksenPerusteenSisalto perusopetuksenPerusteenSisalto) {
        this.perusopetuksenPerusteenSisalto = perusopetuksenPerusteenSisalto;
        this.perusopetuksenPerusteenSisalto.setPeruste(this);
    }

    public void setEsiopetuksenPerusteenSisalto(EsiopetuksenPerusteenSisalto esiopetuksenPerusteenSisalto) {
        this.esiopetuksenPerusteenSisalto = esiopetuksenPerusteenSisalto;
        this.esiopetuksenPerusteenSisalto.setPeruste(this);
    }

    public boolean containsViite(PerusteenOsaViite viite) {
        if (suoritustavat != null) {
            for (Suoritustapa s : suoritustavat) {
                if (s.containsViite(viite)) {
                    return true;
                }
            }
        }

        if (perusopetuksenPerusteenSisalto != null) {
            return perusopetuksenPerusteenSisalto.containsViite(viite);
        }

        if (esiopetuksenPerusteenSisalto != null) {
            return esiopetuksenPerusteenSisalto.containsViite(viite);
        }

        return false;
    }

    public interface Valmis {};

}
