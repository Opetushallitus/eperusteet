package fi.vm.sade.eperusteet.domain.tutkinnonosa_2018;

import fi.vm.sade.eperusteet.domain.Osaamistaso;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "tutkinnonosa_2018.AmmattiatitovaatimuksenOsaamistaso")
@Audited
@Table(name = "ammattitaitovaatimuksenosaamistasot", schema = "tutkinnonosa_2018")
public class AmmattiatitovaatimuksenOsaamistaso {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Osaamistaso osaamistaso;


    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @OrderColumn
//    @JoinTable(name = "osaamistasonkriteeri_tekstipalanen",
//            joinColumns = @JoinColumn(name = "osaamistasonkriteeri_id"),
//            inverseJoinColumns = @JoinColumn(name = "tekstipalanen_id"))
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @BatchSize(size = 25)
    private List<Kriteeri> kriteerit = new ArrayList<>();
}
