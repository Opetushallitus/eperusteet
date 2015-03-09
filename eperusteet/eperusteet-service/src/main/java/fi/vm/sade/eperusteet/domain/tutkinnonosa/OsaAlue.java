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

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.PartialMergeable;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import javax.persistence.CascadeType;
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
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

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
    //@Setter
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    @JoinTable(name = "tutkinnonosa_osaalue_osaamistavoite",
               joinColumns = @JoinColumn(name = "tutkinnonosa_osaalue_id"),
               inverseJoinColumns = @JoinColumn(name = "osaamistavoite_id"))
    @OrderColumn
    private List<Osaamistavoite> osaamistavoitteet;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    /**
     * Jos osa-alueesta on vain yksi kieliversio, määritellään se tässä.
     */
    private Kieli kieli;

    public OsaAlue() {
    }

    public OsaAlue(OsaAlue o) {
        this.nimi = o.nimi;
        this.osaamistavoitteet = new ArrayList<>();
        IdentityHashMap<Osaamistavoite, Osaamistavoite> identityMap = new IdentityHashMap<>();
        for ( Osaamistavoite ot : o.getOsaamistavoitteet() ) {
            if ( identityMap.containsKey(ot) ) {
                this.osaamistavoitteet.add(identityMap.get(ot));
            } else {
                Osaamistavoite t = new Osaamistavoite(ot, identityMap);
                identityMap.put(ot, t);
                this.osaamistavoitteet.add(t);
            }
        }
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

            if (updated.getOsaamistavoitteet() != null) {

                this.setOsaamistavoitteet(mergeOsaamistavoitteet(this.getOsaamistavoitteet(), updated.getOsaamistavoitteet()));
            }
        }
    }

    @Override
    public void partialMergeState(OsaAlue updated) {
        if (updated != null) {
            this.setNimi(updated.getNimi());
        }
    }

    public boolean structureEquals(OsaAlue other) {
        boolean result = refXnor(getNimi(), other.getNimi());
        result &= refXnor(getOsaamistavoitteet(), other.getOsaamistavoitteet());
        if ( result && getOsaamistavoitteet() != null ) {
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
