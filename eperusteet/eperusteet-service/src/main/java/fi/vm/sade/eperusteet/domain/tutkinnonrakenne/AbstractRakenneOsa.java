package fi.vm.sade.eperusteet.domain.tutkinnonrakenne;

import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.ReferenceableEntity;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.dto.Reference;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@Entity
@Table(name = "tutkinnon_rakenne")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tyyppi")
@Audited
public abstract class AbstractRakenneOsa implements Serializable, ReferenceableEntity {

    @Getter
    @NotNull
    @Column(updatable = false, unique = true, nullable = false)
    private UUID tunniste;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Koodi vieras;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.NORMAL)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Getter
    @Setter
    @Audited(targetAuditMode = NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @Getter
    @Setter
    private Boolean pakollinen = false;

    public Boolean getPakollinen() {
        return pakollinen != null ? pakollinen : false;
    }

    public AbstractRakenneOsa() {
    }

    @PrePersist
    public void prePersist() {
        if (this.tunniste == null) {
            this.tunniste = UUID.randomUUID();
        }
    }

    @Override
    public Reference getReference() {
        return new Reference(id);
    }

    public void setTunniste(UUID t) {
        if (t != null && this.tunniste == null) {
            this.tunniste = t;
        }
    }

    public Optional<RakenneOsaVirhe> isSame(AbstractRakenneOsa other, boolean includeText) {
        return this.isSame(other, 0, includeText);
    }

    public Optional<RakenneOsaVirhe> isSame(AbstractRakenneOsa other, int depth, boolean includeText) {
        if (other == null) {
            return fail();
        }

        if (this == other) {
            return success();
        }

        if (!getPakollinen().equals(other.getPakollinen())) {
            return fail("ryhman-pakollisuutta-ei-voi-muuttaa");
        }
        else if (!Objects.equals(tunniste, other.getTunniste())) {
            return fail("ryhman-tunnistetta-ei-voi-muuttaa");
        }
        else if (includeText && !Objects.equals(kuvaus, other.getKuvaus())) {
            return fail("ryhman-kuvausta-ei-voi-muuttaa");
        }

        return success();
    }

    static public class RakenneOsaVirhe {
        @Getter
        String message = "";

        protected RakenneOsaVirhe() {
        }

        protected RakenneOsaVirhe(String message) {
            this.message = message;
        }
    }

    static protected Optional<RakenneOsaVirhe> fail() {
        return Optional.of(new RakenneOsaVirhe("vain-tekstimuutokset-sallittu"));
    }

    static protected Optional<RakenneOsaVirhe> fail(String reason) {
        return Optional.of(new RakenneOsaVirhe(reason));
    }

    static protected Optional<RakenneOsaVirhe> success() {
        return Optional.empty();
    }

}
