package fi.vm.sade.eperusteet.domain.yl;

import fi.vm.sade.eperusteet.domain.AbstractReferenceableEntity;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Entity
@Audited
@Table(name="yl_kohdealue")
public class OpetuksenKohdealue extends AbstractReferenceableEntity {

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @NotNull(message = "Tavoitealueella täytyy olla nimi")
    private TekstiPalanen nimi;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    private TekstiPalanen kuvaus;

    @Getter
    @RelatesToPeruste
    @NotAudited
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "yl_oppiaine_yl_kohdealue",
            joinColumns = @JoinColumn(name = "kohdealueet_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "yl_oppiaine_id", nullable = false))
    private Set<Oppiaine> oppiaineet;

    @Getter
    @RelatesToPeruste
    @NotAudited
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "yl_opetuksen_tavoite_yl_kohdealue",
            joinColumns = @JoinColumn(name = "kohdealueet_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "yl_opetuksen_tavoite_id", nullable = false))
    private Set<OpetuksenTavoite> opetuksenTavoitteet;

    public OpetuksenKohdealue kloonaa() {
        OpetuksenKohdealue klooni = new OpetuksenKohdealue();
        klooni.setKuvaus(kuvaus);
        klooni.setNimi(nimi);
        return klooni;
    }

    public void addOppiaine(Oppiaine oppiaine) {
        if (oppiaineet == null) {
            oppiaineet = new HashSet<>();
        }

        oppiaineet.add(oppiaine);
    }
}
