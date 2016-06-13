package fi.vm.sade.eperusteet.domain.ammattitaitovaatimukset;

import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import java.io.Serializable;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 * Created by autio on 20.10.2015.
 */
@Entity
@Table(name = "ammattitaitovaatimus")
@Audited
public class Ammattitaitovaatimus implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen selite;

    @Getter
    @Setter
    @Column(name = "koodi")
    private String ammattitaitovaatimusKoodi;

    @Getter
    @Setter
    private Integer jarjestys;

    @RelatesToPeruste
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private AmmattitaitovaatimuksenKohde ammattitaitovaatimuksenkohde;

    public Ammattitaitovaatimus() {
    }

    public Ammattitaitovaatimus(AmmattitaitovaatimuksenKohde owner, Ammattitaitovaatimus other) {
        this.selite = other.selite;
        this.ammattitaitovaatimusKoodi = other.ammattitaitovaatimusKoodi;
        this.jarjestys = other.jarjestys;
        this.ammattitaitovaatimuksenkohde = owner;
    }
}
