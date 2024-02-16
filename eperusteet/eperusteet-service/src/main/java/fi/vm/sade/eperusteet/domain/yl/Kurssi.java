package fi.vm.sade.eperusteet.domain.yl;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml.WhitelistType;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Table(name = "yl_kurssi", schema = "public")
@Audited
@Inheritance(strategy = InheritanceType.JOINED)
public class Kurssi extends AbstractAuditedReferenceableEntity implements NimettyKoodillinen {

    @Getter
    @Column(nullable = false, unique = true, updatable = false)
    private UUID tunniste = UUID.randomUUID();

    @Getter
    @Setter
    @NotNull
    @ValidHtml(whitelist = WhitelistType.MINIMAL)
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "nimi_id", nullable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    protected TekstiPalanen nimi;

    @Getter
    @Setter
    @ValidHtml
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "kuvaus_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    protected TekstiPalanen kuvaus;

    @Getter
    @Setter
    @Column(name = "koodi_uri")
    protected String koodiUri;

    @Getter
    @Setter
    @Column(name = "koodi_arvo")
    protected String koodiArvo;

    @Getter
    @Audited
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(name = "yl_kurssi_toteuttava_oppiaine",
            joinColumns = @JoinColumn(name = "kurssi_id", nullable = false, updatable = false),
            inverseJoinColumns = @JoinColumn(name = "oppiaine_id", nullable = false, updatable = false))
    protected Set<Oppiaine> toteuttavatOppiaineet = new HashSet<>(0);
}
