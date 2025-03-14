package fi.vm.sade.eperusteet.domain;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@Immutable
@NoArgsConstructor
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

    static public String getOrDefault(TekstiPalanen tk, Kieli kieli, String otherwise) {
        if (tk == null) {
            return otherwise;
        }
        return tk.getTeksti().getOrDefault(kieli, otherwise);
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

    public static TekstiPalanen empty() {
        return of(new HashMap<>(), null);
    }

    public static TekstiPalanen of(Map<Kieli, String> tekstit) {
        return of(tekstit, null);
    }

    public static TekstiPalanen of(Kieli kieli, String teksti) {
        return of(Collections.singletonMap(kieli, teksti));
    }

    public static TekstiPalanen of(String suomeksi, String ruotsiksi) {
        Map<Kieli, String> tekstit = new HashMap<>();
        tekstit.put(Kieli.FI, suomeksi);
        tekstit.put(Kieli.SV, ruotsiksi);
        return of(tekstit);
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
        tarkistaTekstipalanen(nimi, palanen, pakolliset, virheellisetKielet, true, true);
    }

    static public void tarkistaTekstipalanen(final String nimi, final TekstiPalanen palanen,
                                             final Set<Kieli> pakolliset, Map<String, String> virheellisetKielet, boolean kaikkiPakollisia) {
        tarkistaTekstipalanen(nimi, palanen, pakolliset, virheellisetKielet, !kaikkiPakollisia, kaikkiPakollisia);
    }

    static public boolean tarkistaTekstipalanen(final String nimi, final TekstiPalanen palanen,
                                      final Set<Kieli> pakolliset,
                                      Map<String, String> virheellisetKielet, boolean salliTyhja, boolean kaikkiPakollisia) {
        if (palanen == null || palanen.getTeksti() == null) {
            if (!salliTyhja) {
                for (Kieli kieli : pakolliset) {
                    virheellisetKielet.put(nimi, kieli.name());
                }
                return false;
            }
            return true;
        }

        if (!kaikkiPakollisia) {
            for (Kieli kieli : pakolliset) {
                String osa = palanen.getTeksti().get(kieli);
                if (osa != null && !osa.isEmpty()) {
                    return true;
                }
            }
        }

        if (kaikkiPakollisia) {
            for (Kieli kieli : pakolliset) {
                Map<Kieli, String> teksti = palanen.getTeksti();
                if (!teksti.containsKey(kieli) || teksti.get(kieli) == null || teksti.get(kieli).isEmpty()) {
                    virheellisetKielet.put(nimi, kieli.name());
                    return false;
                }
            }
        }

        return true;
    }
}
