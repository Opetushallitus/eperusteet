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

package fi.vm.sade.eperusteet.domain.tutkinnonosa;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidKoodisto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoUriArvo;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

/**
 *
 * @author harrik
 */
@Entity
@Table(name = "tutkinnonosa_osaalue")
@Audited
public class OsaAlue implements Serializable, PartialMergeable<OsaAlue> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen nimi;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private OsaAlueTyyppi tyyppi;

    @Getter
    @Setter
    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    private GeneerinenArviointiasteikko geneerinenArviointiasteikko;

    @Setter
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private Osaamistavoite pakollisetOsaamistavoitteet;

    @Setter
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private Osaamistavoite valinnaisetOsaamistavoitteet;

    // Ei käytössä Valma/Telma perusteissa
    @Deprecated
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinTable(name = "tutkinnonosa_osaalue_osaamistavoite",
            joinColumns = @JoinColumn(name = "tutkinnonosa_osaalue_id"),
            inverseJoinColumns = @JoinColumn(name = "osaamistavoite_id"))
    @OrderColumn
    private List<Osaamistavoite> osaamistavoitteet;

    @Getter
    @Setter
    @RelatesToPeruste
    @NotAudited
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "tutkinnonosa_tutkinnonosa_osaalue",
            inverseJoinColumns = @JoinColumn(name = "tutkinnonosa_id"),
            joinColumns = @JoinColumn(name = "tutkinnonosa_osaalue_id"))
    private Set<TutkinnonOsa> tutkinnonOsat;

    @Getter
    @Setter
    @OneToOne(cascade = {CascadeType.ALL})
    private ValmaTelmaSisalto valmaTelmaSisalto;

    /**
     * Jos osa-alueesta on vain yksi kieliversio, määritellään se tässä.
     */
    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private Kieli kieli;

    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @ValidKoodisto(koodisto = KoodistoUriArvo.AMMATILLISENOPPIAINEET)
    private Koodi koodi;

    public OsaAlue() {
    }

    public OsaAlue(OsaAlue o) {
        this.nimi = o.nimi;
        this.kuvaus = o.kuvaus;
        this.osaamistavoitteet = new ArrayList<>();
        this.valmaTelmaSisalto = null;
        this.koodi = o.koodi;
        this.kieli = o.kieli;

        IdentityHashMap<Osaamistavoite, Osaamistavoite> identityMap = new IdentityHashMap<>();
        if (CollectionUtils.isNotEmpty(o.getOsaamistavoitteet())) {
            for (Osaamistavoite ot : o.getOsaamistavoitteet()) {
                if (identityMap.containsKey(ot)) {
                    this.osaamistavoitteet.add(identityMap.get(ot));
                } else {
                    Osaamistavoite t = new Osaamistavoite(ot, identityMap);
                    identityMap.put(ot, t);
                    this.osaamistavoitteet.add(t);
                }
            }
        }
    }

    public Osaamistavoite getValinnaisetOsaamistavoitteet() {
        if (OsaAlueTyyppi.OSAALUE2020.equals(this.tyyppi)) {
            return valinnaisetOsaamistavoitteet;
        }
        return null;
    }

    public Osaamistavoite getPakollisetOsaamistavoitteet() {
        if (OsaAlueTyyppi.OSAALUE2020.equals(this.tyyppi)) {
            return pakollisetOsaamistavoitteet;
        }
        return null;
    }

    public List<Osaamistavoite> getOsaamistavoitteet() {
        if (this.tyyppi == null || OsaAlueTyyppi.OSAALUE2014.equals(this.tyyppi)) {
            return osaamistavoitteet;
        }
        return null;
    }

    public void setOsaamistavoitteet(List<Osaamistavoite> osaamistavoitteet) {
        if (this.osaamistavoitteet == null) {
            this.osaamistavoitteet = new ArrayList<>();
        }
        this.osaamistavoitteet.clear();
        if (osaamistavoitteet != null) {
            this.osaamistavoitteet.addAll(osaamistavoitteet);
        }
    }

    @Override
    public void mergeState(OsaAlue updated) {
        if (updated != null) {
            this.setNimi(updated.getNimi());
            this.setKuvaus(updated.getKuvaus());
            this.setTyyppi(updated.getTyyppi());
            this.koodi = updated.getKoodi();
            this.setGeneerinenArviointiasteikko(updated.getGeneerinenArviointiasteikko());
            this.setPakollisetOsaamistavoitteet(updated.getPakollisetOsaamistavoitteet());
            this.setValinnaisetOsaamistavoitteet(updated.getValinnaisetOsaamistavoitteet());
            this.setKieli(updated.kieli);

            if (updated.getOsaamistavoitteet() != null) {
                this.setOsaamistavoitteet(mergeOsaamistavoitteet(this.getOsaamistavoitteet(), updated.getOsaamistavoitteet()));
            }
        }
    }

    @Override
    public void partialMergeState(OsaAlue updated) {
        if (updated != null) {
            this.setNimi(updated.getNimi());
            this.setKuvaus(updated.getKuvaus());
        }
    }

    public boolean structureEquals(OsaAlue other) {
        boolean result = refXnor(getNimi(), other.getNimi());
        result &= refXnor(getKuvaus(), other.getKuvaus());
        if ( result && getOsaamistavoitteet() != null && other.getOsaamistavoitteet() != null ) {
            Iterator<Osaamistavoite> i = getOsaamistavoitteet().iterator();
            Iterator<Osaamistavoite> j = other.getOsaamistavoitteet().iterator();
            while (result && i.hasNext() && j.hasNext()) {
                result &= i.next().structureEquals(j.next());
            }
            result &= !i.hasNext();
            result &= !j.hasNext();
        }
        return result;
    }

    private List<Osaamistavoite> mergeOsaamistavoitteet(List<Osaamistavoite> current, List<Osaamistavoite> updated) {
        List<Osaamistavoite> tempList = new ArrayList<>();
        boolean loyty = false;
        if (updated != null) {
            for (Osaamistavoite osaamistavoiteUpdate : updated) {
                for (Osaamistavoite osaamistavoiteCurrent : current) {
                    if (osaamistavoiteCurrent.getId().equals(osaamistavoiteUpdate.getId())) {
                        // Jos osa-alueella osaamistavoitelista mergessä, niin kyseessä on kevyempi
                        // osaamistavoite objekteja. Joten käytetään partialMergeStatea.
                        //osaamistavoiteCurrent.partialMergeState(osaamistavoiteUpdate);
                        osaamistavoiteCurrent.mergeState(osaamistavoiteUpdate);
                        tempList.add(osaamistavoiteCurrent);
                        loyty = true;
                    }
                }
                if (!loyty) {
                    tempList.add(osaamistavoiteUpdate);
                }
                loyty = false;
            }
        }
        return tempList;
    }
}
