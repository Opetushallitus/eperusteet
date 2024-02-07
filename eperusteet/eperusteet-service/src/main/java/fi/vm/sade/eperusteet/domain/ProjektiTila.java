package fi.vm.sade.eperusteet.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

public enum ProjektiTila {
    POISTETTU("poistettu") {
        @Override
        public Set<ProjektiTila> mahdollisetTilat(PerusteTyyppi tyyppi) {
            return EnumSet.of(LAADINTA);
        }
    },
    LAADINTA("laadinta") {
        @Override
        public Set<ProjektiTila> mahdollisetTilat(PerusteTyyppi tyyppi) {
            if (tyyppi.equals(PerusteTyyppi.POHJA)) {
                return EnumSet.of(POISTETTU, VALMIS);
            } else {
                return EnumSet.of(KOMMENTOINTI, VIIMEISTELY, POISTETTU);
            }
        }
    },
    KOMMENTOINTI("kommentointi") {
        @Override
        public Set<ProjektiTila> mahdollisetTilat(PerusteTyyppi tyyppi) {
            return EnumSet.of(LAADINTA, POISTETTU);
        }
    },
    VIIMEISTELY("viimeistely") {
        @Override
        public Set<ProjektiTila> mahdollisetTilat(PerusteTyyppi tyyppi) {
            return EnumSet.of(LAADINTA, VALMIS, POISTETTU);
        }
    },
    VALMIS("valmis") {
        @Override
        public Set<ProjektiTila> mahdollisetTilat(PerusteTyyppi tyyppi) {
            if (tyyppi.equals(PerusteTyyppi.POHJA)) {
                return EnumSet.of(POISTETTU);
            } else {
                return EnumSet.of(LAADINTA, VIIMEISTELY, JULKAISTU, POISTETTU);
            }
        }
    },
    JULKAISTU("julkaistu") {
        @Override
        public Set<ProjektiTila> mahdollisetTilat(PerusteTyyppi tyyppi) {
            // EP-1387
            if (tyyppi.equals(PerusteTyyppi.OPAS)) {
                return EnumSet.of(LAADINTA, POISTETTU);
            } else {
                return EnumSet.of(POISTETTU);
            }
        }
    };

    public static ProjektiTila[] jalkeen(ProjektiTila tila) {
        return Stream.of(ProjektiTila.values()).filter(t -> t.ordinal() > tila.ordinal()).toArray(ProjektiTila[]::new);
    }

    private final String tila;

    ProjektiTila(String tila) {
        this.tila = tila;
    }

    @Override
    public String toString() {
        return tila;
    }

    @JsonCreator
    public static ProjektiTila of(String tila) {
        for (ProjektiTila s : values()) {
            if (s.tila.equalsIgnoreCase(tila)) {
                return s;
            }
        }
        throw new IllegalArgumentException(tila + " ei ole kelvollinen ProjektiTila");
    }

    public Set<ProjektiTila> mahdollisetTilat(PerusteTyyppi tyyppi) {
        return EnumSet.noneOf(ProjektiTila.class);
    }

    public static Set<ProjektiTila> kaytossaOlevatTilat() {
        return EnumSet.of(LAADINTA, VIIMEISTELY, JULKAISTU, VALMIS, KOMMENTOINTI);
    }


    public boolean isOneOf(ProjektiTila... tilat) {
        for (ProjektiTila toinen : tilat) {
            if (toinen.toString().equals(this.tila)) {
                return true;
            }
        }
        return false;
    }
}
