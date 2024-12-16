package fi.vm.sade.eperusteet.domain.yl.lukio;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml.WhitelistType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import jakarta.persistence.*;
import java.util.UUID;
import java.util.function.Predicate;

@Entity
@Audited
@Table(name = "yl_aihekokonaisuus", schema = "public")
public class Aihekokonaisuus extends AbstractAuditedReferenceableEntity {

    public static Predicate<Aihekokonaisuus> inPeruste(long perusteId) {
        return aihekokonaisuus -> aihekokonaisuus.getAihekokonaisuudet().getSisalto().getPeruste().getId().equals(perusteId);
    }

    @Column(nullable = false, unique = true, updatable = false)
    @Getter
    private UUID tunniste = UUID.randomUUID();

    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml(whitelist = WhitelistType.MINIMAL)
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "otsikko_id", nullable = true)
    private TekstiPalanen otsikko;

    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "yleiskuvaus_id")
    private TekstiPalanen yleiskuvaus;

    @Getter
    @Setter
    private Long jnro;

    @RelatesToPeruste
    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE})
    @JoinColumn(name = "aihekokonaisuudet_id", nullable = false)
    private Aihekokonaisuudet aihekokonaisuudet;

    public Aihekokonaisuus kloonaa() {
        Aihekokonaisuus klooni = new Aihekokonaisuus();
        klooni.setJnro(this.getJnro());
        klooni.setOtsikko(this.getOtsikko());
        klooni.setAihekokonaisuudet(this.getAihekokonaisuudet());
        klooni.setYleiskuvaus(this.getYleiskuvaus());

        return klooni;
    }
}
