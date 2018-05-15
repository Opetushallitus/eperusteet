package fi.vm.sade.eperusteet.domain.reformi;

import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "tutkinnonosan_ammattitaitovaatimukset")
@Audited
public class TutkinnonOsanAmmattitaitovaatimukset {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    private TekstiPalanen kohde;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    private TekstiPalanen arvioinninKohde;

//    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
//    @JoinTable(name = "ammattitaitovaatimus_v2",
//            joinColumns = @JoinColumn(name = "tutkinnonosan_ammattitaitovaatimukset_id"),
//            inverseJoinColumns = @JoinColumn(name = "ammattitaitovaatimus_v2_id"))
//    @OrderColumn(name = "ammattitaitovaatimus_order")
//    @Getter
//    @Setter
//    private List<AmmattitaitovaatimusV2> ammattitaitovaatimukset;

}
