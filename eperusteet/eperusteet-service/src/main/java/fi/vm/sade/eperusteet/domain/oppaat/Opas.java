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

package fi.vm.sade.eperusteet.domain.oppaat;

import fi.vm.sade.eperusteet.domain.AbstractAuditedEntity;
import fi.vm.sade.eperusteet.domain.Diaarinumero;
import fi.vm.sade.eperusteet.domain.ReferenceableEntity;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author nkala
 */
@Entity
@Table(name = "oppaat")
public class Opas extends AbstractAuditedEntity implements Serializable, ReferenceableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    @NotNull(message="Nimi ei voi olla tyhjä")
    private String projektinNimi;

    @Getter
    @Setter
    @Column(unique = true)
    private Diaarinumero diaarinumero;

    @Getter
    @Setter
    @Column(name = "ryhmaoid")
    private String ryhmaOid;

    @Getter
    @Setter
    @NotNull
    private boolean esikatseltavissa = false;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @NotNull
    private OpasTila tila = OpasTila.LAADINTA;

    @ValidHtml(whitelist = WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
//    @NotNull(groups = Valmis.class)
    private TekstiPalanen nimi;

    @ValidHtml(whitelist = WhitelistType.SIMPLIFIED)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    private Maarayskirje maarayskirje;

    @Getter
    @Setter
    @NotNull
    private boolean koulutusvienti = false;

    @Getter
    @Setter
    @OrderColumn
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Muutosmaarays> muutosmaaraykset = new ArrayList<>();

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
    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
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

    @Temporal(TemporalType.TIMESTAMP)
    @Getter
    @Setter
    private Date paatospvm;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapKey(name = "suoritustapakoodi")
    @JoinTable(name = "peruste_suoritustapa",
               joinColumns = @JoinColumn(name = "peruste_id"),
               inverseJoinColumns = @JoinColumn(name = "suoritustapa_id"))
    @Getter
    @Setter
    private Set<Suoritustapa> suoritustavat = new HashSet<>();

    @Getter
    @OneToOne(mappedBy = "peruste", optional = true, fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private PerusopetuksenPerusteenSisalto perusopetuksenPerusteenSisalto;

    @Getter
    @OneToOne(mappedBy = "peruste", optional = true, fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private EsiopetuksenPerusteenSisalto esiopetuksenPerusteenSisalto;

    @Getter
    @OneToOne(mappedBy = "peruste", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private LukiokoulutuksenPerusteenSisalto lukiokoulutuksenPerusteenSisalto;

    @Getter
    @OneToOne(mappedBy = "peruste", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private AIPEOpetuksenSisalto aipeOpetuksenPerusteenSisalto;

    @Getter
    @Enumerated(EnumType.STRING)
    @NotNull
    private PerusteTila tila = PerusteTila.LUONNOS;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @NotNull
    private PerusteTyyppi tyyppi = PerusteTyyppi.NORMAALI;

    @Getter
    @OneToOne(mappedBy = "peruste", fetch = FetchType.LAZY, cascade = { CascadeType.ALL })
    private KVLiite kvliite;

    public void setKvliite(KVLiite liite) {
        if (liite != null) {
            this.kvliite = liite;
            liite.setPeruste(this);
        }
    }

    /**
     * Kielet jolla peruste tarjotaan. Oletuksena suomi ja ruotsi.
     */
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    @CollectionTable(name = "peruste_kieli")
    @Column(name = "kieli")
    @Size(min = 1, groups = Valmis.class)
    private Set<Kieli> kielet = EnumSet.of(Kieli.FI, Kieli.SV);

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "peruste_liite", inverseJoinColumns = {@JoinColumn(name="liite_id")}, joinColumns = {@JoinColumn(name="peruste_id")})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Set<Liite> liitteet = new HashSet<>();

    public void attachLiite(Liite liite) {
        liitteet.add(liite);
    }

    public void removeLiite(Liite liite) {
        liitteet.remove(liite);
    }

    public Suoritustapa getSuoritustapa(Suoritustapakoodi koodi) {
        for (Suoritustapa s : suoritustavat) {
            if (s.getSuoritustapakoodi() == koodi) {
                return s;
            }
        }

        if (koodi.equals(Suoritustapakoodi.REFORMI)) {
            Optional<Suoritustapa> naytto = suoritustavat.stream()
                    .filter(suoritustapa -> suoritustapa.getSuoritustapakoodi().equals(Suoritustapakoodi.NAYTTO))
                    .findFirst();
            if (naytto.isPresent()) {
                return naytto.get();
            }
        }

        throw new IllegalArgumentException("Perusteella ei ole pyydettyä suoritustapaa");
    }

    @Override
    public void asetaTila(OpasTila tila) {
        this.tila = tila;
    }

    @Override
    public EntityReference getReference() {
        return new EntityReference(id);
    }

    public void setMuutosmaaraykset(List<Muutosmaarays> muutosmaaraykset) {
        this.muutosmaaraykset.clear();
        if (muutosmaaraykset != null) {
            this.muutosmaaraykset.addAll(muutosmaaraykset);
        }
    }

    /*
    Palauttaa suoritustavan mukaisen sisällön.
    */
    public PerusteenOsaViite getSisalto(Suoritustapakoodi suoritustapakoodi) {
        PerusteenOsaViite viite = null;
        switch (suoritustapakoodi) {
            case ESIOPETUS:
            case LISAOPETUS:
            case VARHAISKASVATUS:
                viite = this.getEsiopetuksenPerusteenSisalto().getSisalto();
                break;
            case PERUSOPETUS:
                viite = this.getPerusopetuksenPerusteenSisalto().getSisalto();
                break;
            case AIPE:
                viite = this.getAipeOpetuksenPerusteenSisalto().getSisalto();
                break;
            case LUKIOKOULUTUS:
                viite = this.getLukiokoulutuksenPerusteenSisalto().getSisalto();
                break;
            default:
                // Ammatilliset
                for(Suoritustapa suoritustapa : this.getSuoritustavat()) {
                    if (suoritustapa.getSuoritustapakoodi().equals(suoritustapakoodi)) {
                        viite = suoritustapa.getSisalto();
                    }
                }   break;
        }
        return viite;
    }

    public void setPerusopetuksenPerusteenSisalto(PerusopetuksenPerusteenSisalto perusopetuksenPerusteenSisalto) {
        this.perusopetuksenPerusteenSisalto = perusopetuksenPerusteenSisalto;
        this.perusopetuksenPerusteenSisalto.setPeruste(this);
    }

    public void setSisalto(AIPEOpetuksenSisalto sisalto) {
        this.aipeOpetuksenPerusteenSisalto = sisalto;
        this.aipeOpetuksenPerusteenSisalto.setPeruste(this);
    }

    public void setEsiopetuksenPerusteenSisalto(EsiopetuksenPerusteenSisalto esiopetuksenPerusteenSisalto) {
        this.esiopetuksenPerusteenSisalto = esiopetuksenPerusteenSisalto;
        this.esiopetuksenPerusteenSisalto.setPeruste(this);
    }

    public void setLukiokoulutuksenPerusteenSisalto(LukiokoulutuksenPerusteenSisalto lukiokoulutuksenPerusteenSisalto) {
        this.lukiokoulutuksenPerusteenSisalto = lukiokoulutuksenPerusteenSisalto;
        if (lukiokoulutuksenPerusteenSisalto != null) {
            lukiokoulutuksenPerusteenSisalto.setPeruste(this);
        }
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

        if (aipeOpetuksenPerusteenSisalto != null) {
            return aipeOpetuksenPerusteenSisalto.containsViite(viite);
        }

        if (esiopetuksenPerusteenSisalto != null) {
            return esiopetuksenPerusteenSisalto.containsViite(viite);
        }

        if  (lukiokoulutuksenPerusteenSisalto != null
                && lukiokoulutuksenPerusteenSisalto.containsViite(viite)) {
            return lukiokoulutuksenPerusteenSisalto.containsViite(viite);
        }

        throw new BusinessRuleViolationException("Ei toteutusta koulutustyypillä");
    }

}
