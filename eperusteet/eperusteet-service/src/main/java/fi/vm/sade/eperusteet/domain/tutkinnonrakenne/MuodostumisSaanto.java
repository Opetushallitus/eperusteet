package fi.vm.sade.eperusteet.domain.tutkinnonrakenne;

import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
@EqualsAndHashCode
public class MuodostumisSaanto implements Serializable {

    private Laajuus laajuus;
    private Koko koko;

    public MuodostumisSaanto() {
    }

    public MuodostumisSaanto(Laajuus laajuus) {
        this.laajuus = laajuus;
        this.koko = null;
    }

    public MuodostumisSaanto(Koko koko) {
        this.koko = koko;
        this.laajuus = null;
    }

    public MuodostumisSaanto(Laajuus laajuus, Koko koko) {
        this.koko = koko;
        this.laajuus = laajuus;
    }

    public MuodostumisSaanto(MuodostumisSaanto other) {
        if (other != null) {
            this.laajuus = other.getLaajuus() == null ? null : new Laajuus(other.getLaajuus());
            this.koko = other.getKoko() == null ? null : new Koko(other.getKoko());
        }
    }

    public Integer kokoMinimi() {
        return koko != null && koko.getMinimi() != null ? koko.getMinimi() : Integer.MIN_VALUE;
    }

    public Integer kokoMaksimi() {
        return koko != null && koko.getMaksimi() != null ? koko.getMaksimi() : Integer.MIN_VALUE;
    }

    public Integer laajuusMinimi() {
        return laajuus != null && laajuus.getMinimi() != null ? laajuus.getMinimi() : Integer.MIN_VALUE;
    }

    public Integer laajuusMaksimi() {
        return laajuus != null && laajuus.getMaksimi() != null ? laajuus.getMaksimi() : Integer.MIN_VALUE;
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    @Embeddable
    public static class Laajuus implements Serializable {

        @Column(name = "laajuus_min")
        private Integer minimi;
        @Column(name = "laajuus_max")
        private Integer maksimi;

        @Column(name = "laajuus_yksikko")
        @Enumerated(EnumType.STRING)
        private LaajuusYksikko yksikko;

        public Laajuus(Integer minimi, Integer maksimi, LaajuusYksikko yksikko) {
            this.minimi = minimi;
            this.maksimi = maksimi;
            this.yksikko = yksikko;
        }

        public Laajuus(Laajuus copy) {
            this(copy.minimi, copy.maksimi, copy.yksikko);
        }

        public Laajuus() {
        }

    }

    @Getter
    @Setter
    @EqualsAndHashCode
    @Embeddable
    public static class Koko implements Serializable {

        @Column(name = "koko_min")
        private Integer minimi;
        @Column(name = "koko_max")
        Integer maksimi;

        public Koko(Integer minimi, Integer maksimi) {
            this.minimi = minimi;
            this.maksimi = maksimi;
        }

        public Koko() {
        }

        public Koko(Koko copy) {
            this(copy.minimi, copy.maksimi);
        }

    }
}
