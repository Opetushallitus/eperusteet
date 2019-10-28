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
import java.text.Normalizer;
import java.util.*;
import javax.persistence.*;
import lombok.Getter;
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
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
    @Immutable
    @CollectionTable(name = "tekstipalanen_teksti")
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<LokalisoituTeksti> teksti;

    @Getter
    @Column(updatable = false)
    private UUID tunniste;

    protected TekstiPalanen() {
    }

    private TekstiPalanen(Set<LokalisoituTeksti> tekstit, UUID tunniste) {
        this.teksti = tekstit;
        this.tunniste = tunniste != null ? tunniste : UUID.randomUUID();
    }

    public Long getId() {
        return id;
    }

    public Map<Kieli, String> getTeksti() {
        EnumMap<Kieli, String> map = new EnumMap<>(Kieli.class);
        if (teksti != null) {
            for (LokalisoituTeksti t : teksti) {
                map.put(t.getKieli(), t.getTeksti());
            }
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

    public static TekstiPalanen of(Map<Kieli, String> tekstit, UUID tunniste) {
        if (tekstit == null) {
            return null;
        }
        HashSet<LokalisoituTeksti> tmp = new HashSet<>(tekstit.size());
        for (Map.Entry<Kieli, String> e : tekstit.entrySet()) {
            if (e.getValue() != null) {
                String v = Normalizer.normalize(e.getValue().trim(), Normalizer.Form.NFC);
                if (!v.isEmpty()) {
                    tmp.add(new LokalisoituTeksti(e.getKey(), v));
                }
            }
        }
        if (tmp.isEmpty()) {
            return null;
        }
        return new TekstiPalanen(tmp, tunniste);
    }

    public static TekstiPalanen of(TekstiPalanen other) {
        if (other != null) {
            return of(other.getTeksti(), other.getTunniste());
        }
        return null;
    }

    public static TekstiPalanen of(Map<Kieli, String> tekstit) {
        return of(tekstit, null);
    }

    public static TekstiPalanen of(Kieli kieli, String teksti) {
        return of(Collections.singletonMap(kieli, teksti));
    }

    @Override
    public String toString() {
        Map<Kieli, String> tekstit = getTeksti();
        if (tekstit.isEmpty()) {
            return "";
        }
        String fi = tekstit.get(Kieli.FI);
        if (fi != null) {
            return fi;
        }
        return tekstit.entrySet().iterator().next().getValue();
    }

    static public void tarkistaTekstipalanen(final String nimi, final TekstiPalanen palanen,
                                      final Set<Kieli> pakolliset, Map<String, String> virheellisetKielet) {
        tarkistaTekstipalanen(nimi, palanen, pakolliset, virheellisetKielet, false);
    }

    static public void tarkistaTekstipalanen(final String nimi, final TekstiPalanen palanen,
                                      final Set<Kieli> pakolliset,
                                      Map<String, String> virheellisetKielet, boolean pakollinen) {
        if (palanen == null || palanen.getTeksti() == null) {
            return;
        }

        // Oispa lambdat
        boolean onJollainVaaditullaKielella = false;
        if (!pakollinen) {
            for (Kieli kieli : pakolliset) {
                String osa = palanen.getTeksti().get(kieli);
                if (osa != null && !osa.isEmpty()) {
                    onJollainVaaditullaKielella = true;
                    break;
                }
            }
        }

        if (pakollinen || onJollainVaaditullaKielella) {
            for (Kieli kieli : pakolliset) {
                Map<Kieli, String> teksti = palanen.getTeksti();
                if (!teksti.containsKey(kieli) || teksti.get(kieli) == null || teksti.get(kieli).isEmpty()) {
                    virheellisetKielet.put(nimi, kieli.name());
                }
            }
        }
    }
}
