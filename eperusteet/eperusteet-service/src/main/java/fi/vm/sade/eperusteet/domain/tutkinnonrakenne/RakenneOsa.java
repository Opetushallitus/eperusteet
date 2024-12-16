package fi.vm.sade.eperusteet.domain.tutkinnonrakenne;

import com.google.common.base.Objects;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.util.Optional;

@Getter
@Setter
@Entity
@DiscriminatorValue("RO")
@Audited
public class RakenneOsa extends AbstractRakenneOsa {

    @JoinColumn(name = "rakenneosa_tutkinnonosaviite")
    @ManyToOne
    private TutkinnonOsaViite tutkinnonOsaViite;

    private String erikoisuus;

    @Override
    public Optional<RakenneOsaVirhe> isSame(AbstractRakenneOsa other, int depth, boolean excludeText) {
        Optional<RakenneOsaVirhe> supervalidation = super.isSame(other, depth, excludeText);
        if (supervalidation.isPresent()) {
            return supervalidation;
        }

        if (other instanceof RakenneOsa) {
            final RakenneOsa ro = (RakenneOsa) other;
            boolean rakenneOsaValid = this.getPakollinen() == ro.getPakollinen()
                    && Objects.equal(this.tutkinnonOsaViite, ro.getTutkinnonOsaViite())
                    && (erikoisuus == null ? ro.getErikoisuus() == null : erikoisuus.equals(ro.getErikoisuus()));
            if (!rakenneOsaValid) {
                return fail("rakenne-osan-validointi-epaonnistui");
            }
        }

        return success();
    }
}
