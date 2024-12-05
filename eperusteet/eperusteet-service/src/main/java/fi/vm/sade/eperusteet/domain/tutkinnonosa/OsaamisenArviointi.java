package fi.vm.sade.eperusteet.domain.tutkinnonosa;

import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Table(name = "valmatelma_osaamisenarviointi")
@Audited
public class OsaamisenArviointi implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    private Long id;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    private TekstiPalanen kohde;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    private TekstiPalanen selite;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @OrderColumn(name = "arviointi_order")
    @JoinTable(name = "valmatelmaarviointi_tekstipalanen",
            joinColumns = @JoinColumn(name = "valmatelma_osaamisenarviointi_id"),
            inverseJoinColumns = @JoinColumn(name = "tekstipalanen_id"))
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @BatchSize(size = 25)
    @Getter
    @Setter
    private List<TekstiPalanen> tavoitteet = new ArrayList<>();

    public OsaamisenArviointi() {
    }

    public OsaamisenArviointi(OsaamisenArviointi other) {
        this.kohde = other.getKohde();
        this.selite = other.getSelite();
        this.tavoitteet = other.getTavoitteet().stream().collect(Collectors.toList());
    }

}
