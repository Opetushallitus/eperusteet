package fi.vm.sade.eperusteet.dto;

import com.fasterxml.jackson.annotation.*;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Metalink implements Serializable {

    public enum MetalinkTarget {
        PERUSTE("peruste"),
        SUORITUSTAPA("suoritustapa"),
        TUTKINNONOSA("tutkinnonosa"),
        TUTKINNONOSAVIITE("tutkinnonosaviite"),
        PERUSTEENOSAVIITE("perusteenosaviite")
        ;

        private final String tyyppi;

        MetalinkTarget(String tyyppi) {
            this.tyyppi = tyyppi;
        }

        @Override
        public String toString() {
            return tyyppi;
        }

        @JsonCreator
        public static MetalinkTarget of(String tila) {
            for (MetalinkTarget s : values()) {
                if (s.tyyppi.equalsIgnoreCase(tila)) {
                    return s;
                }
            }
            throw new IllegalArgumentException(tila + " ei ole kelvollinen tila");
        }
    }

    private MetalinkTarget tyyppi;
    private Long perusteId;
    Suoritustapakoodi suoritustapa;
    private Long tutkinnonOsaId;
    private Long tutkinnonOsaViiteId;
    private Long perusteenOsaViiteId;
    private Long perusteenOsaId;

    static public Metalink fromPeruste(Long perusteId) {
        Metalink result = new Metalink();
        result.tyyppi = MetalinkTarget.PERUSTE;
        result.perusteId = perusteId;
        return result;
    }

    public static Metalink fromSuoritustapa(Long perusteId, Suoritustapakoodi suoritustapakoodi) {
        Metalink result = new Metalink();
        result.tyyppi = MetalinkTarget.SUORITUSTAPA;
        result.suoritustapa = suoritustapakoodi;
        result.perusteId = perusteId;
        return result;
    }

    public static Metalink fromPerusteenOsaViite(Long perusteId, Suoritustapakoodi suoritustapakoodi, Long povId, Long toId) {
        Metalink result = new Metalink();
        result.tyyppi = MetalinkTarget.PERUSTEENOSAVIITE;
        result.suoritustapa = suoritustapakoodi;
        result.perusteId = perusteId;
        result.perusteenOsaViiteId = povId;
        result.perusteenOsaId = toId;
        return result;
    }

    public static Metalink fromTutkinnonOsaViite(Long perusteId, Suoritustapakoodi suoritustapakoodi, Long tovId, Long toId) {
        Metalink result = new Metalink();
        result.tyyppi = MetalinkTarget.TUTKINNONOSAVIITE;
        result.suoritustapa = suoritustapakoodi;
        result.perusteId = perusteId;
        result.tutkinnonOsaViiteId = tovId;
        result.tutkinnonOsaId = toId;
        return result;
    }

    public static Metalink fromTutkinnonOsa(Long id) {
        Metalink result = new Metalink();
        result.tyyppi = MetalinkTarget.TUTKINNONOSA;
        result.tutkinnonOsaId = id;
        return result;
    }

}
