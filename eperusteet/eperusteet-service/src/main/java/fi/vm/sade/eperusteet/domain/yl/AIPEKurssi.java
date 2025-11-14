package fi.vm.sade.eperusteet.domain.yl;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.HistoriaTapahtuma;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.Tunnistettava;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import jakarta.persistence.*;

import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Table(name = "yl_aipe_kurssi", schema = "public")
@Audited
public class AIPEKurssi extends AbstractAuditedReferenceableEntity implements AIPEJarjestettava, Tunnistettava, HistoriaTapahtuma {

    @Getter
    @Column(nullable = false, unique = true, updatable = false)
    private UUID tunniste = UUID.randomUUID();

    @Getter
    @Setter
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen nimi;

    @Getter
    @Setter
    @ValidHtml
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "kuvaus_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @Getter
    @Setter
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Koodi koodi;

    @Getter
    @Setter
    private Integer jarjestys;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @NotAudited
    @JoinTable(name = "aipeoppiaine_aipekurssi",
            joinColumns = { @JoinColumn(name = "kurssi_id")},
            inverseJoinColumns = { @JoinColumn(name = "oppiaine_id")})
    private AIPEOppiaine oppiaine;

    @ManyToMany
    @JoinTable(name = "yl_aipe_kurssi_yl_opetuksen_tavoite",
            joinColumns = @JoinColumn(name = "yl_aipe_kurssi_id"),
            inverseJoinColumns = @JoinColumn(name = "tavoitteet_id"))
    private Set<OpetuksenTavoite> tavoitteet = new HashSet<>();

    public Set<OpetuksenTavoite> getTavoitteet() {
        return tavoitteet;
    }

    public void setTavoitteet(Set<OpetuksenTavoite> tavoitteet) {
        this.tavoitteet = tavoitteet;
    }

    public static void validateChange(AIPEKurssi a, AIPEKurssi b) {
        Koodi.validateChange(a.koodi, b.koodi);

        if (a.nimi != null && b.nimi == null) {
            throw new BusinessRuleViolationException("nimea-ei-voi-poistaa");
        }

        if (a.kuvaus != null && b.kuvaus == null) {
            throw new BusinessRuleViolationException("nimea-ei-voi-poistaa");
        }

    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.aipekurssi;
    }
}
