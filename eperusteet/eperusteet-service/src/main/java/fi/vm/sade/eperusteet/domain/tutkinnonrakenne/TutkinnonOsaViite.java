package fi.vm.sade.eperusteet.domain.tutkinnonrakenne;

import fi.vm.sade.eperusteet.domain.HistoriaTapahtuma;
import fi.vm.sade.eperusteet.domain.ReferenceableEntity;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.Sortable;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "tutkinnonosaviite")
@Audited
public class TutkinnonOsaViite implements ReferenceableEntity, Serializable, Sortable, HistoriaTapahtuma {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private Suoritustapa suoritustapa;

    @Getter
    @Setter
    @Column(precision = 10, scale = 2)
    private BigDecimal laajuus;

    @Getter
    @Setter
    @Column(name = "laajuus_maksimi", precision = 10, scale = 2)
    private BigDecimal laajuusMaksimi;

    @Getter
    @Setter
    private Integer jarjestys;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @NotNull
    private TutkinnonOsa tutkinnonOsa;

    @Column
    @Getter
    @Setter
    @Temporal(TemporalType.TIMESTAMP)
    private Date muokattu;

    @Override
    public Reference getReference() {
        return new Reference(getId());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.suoritustapa);
        hash = 97 * hash + Objects.hashCode(this.tutkinnonOsa);
        return hash;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof TutkinnonOsaViite) {
            final TutkinnonOsaViite other = (TutkinnonOsaViite) that;
            if (!Objects.equals(this.suoritustapa, other.suoritustapa)) {
                return false;
            }
            if (!Objects.equals(this.tutkinnonOsa, other.tutkinnonOsa)) {
                return false;
            }
            return true;
        }
        return false;
    }


    @Override
    public Date getLuotu() {
        return this.tutkinnonOsa.getLuotu();
    }

    @Override
    public Date getMuokattu() {
        return this.tutkinnonOsa.getMuokattu();
    }

    @Override
    public String getLuoja() {
        return this.tutkinnonOsa.getLuoja();
    }

    @Override
    public String getMuokkaaja() {
        return this.tutkinnonOsa.getMuokkaaja();
    }

    @Override
    public TekstiPalanen getNimi() {
        return this.tutkinnonOsa.getNimi();
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.tutkinnonosa;
    }
}
