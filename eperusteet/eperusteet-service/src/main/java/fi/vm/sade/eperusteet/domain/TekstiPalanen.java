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
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Cacheable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

/**
 *
 * @author jhyoty
 */
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@Immutable
@Table(name = "tekstipalanen")
public class TekstiPalanen implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
    @Immutable
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<LokalisoituTeksti> teksti;

    protected TekstiPalanen() {
    }

    public TekstiPalanen(Map<Kieli, String> tekstit) {
        teksti = new HashSet<>(tekstit.size());
        for ( Map.Entry<Kieli, String> e : tekstit.entrySet() ) {
            teksti.add(new LokalisoituTeksti(e.getKey(), e.getValue()));
        }
    }

    public Long getId() {
        return id;
    }

    public Map<Kieli,String> getTeksti() {
        EnumMap<Kieli,String> map = new EnumMap<>(Kieli.class);
        for ( LokalisoituTeksti t : teksti ) {
            map.put(t.getKieli(), t.getTeksti());
        }
        return map;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.teksti);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof TekstiPalanen) {
            final TekstiPalanen other = (TekstiPalanen) obj;
            return Objects.equals(this.teksti, other.teksti);
        }
        return false;
    }

}
