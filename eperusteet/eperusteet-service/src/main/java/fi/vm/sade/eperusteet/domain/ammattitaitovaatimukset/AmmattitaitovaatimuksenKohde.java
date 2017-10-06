package fi.vm.sade.eperusteet.domain.ammattitaitovaatimukset;

import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 * Created by autio on 20.10.2015.
 */
@Entity
@Table(name = "ammattitaitovaatimuksenkohde")
@Audited
public class AmmattitaitovaatimuksenKohde implements Serializable {

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
    private TekstiPalanen otsikko;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen selite;

    @RelatesToPeruste
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @Getter
    @Setter
    private AmmattitaitovaatimuksenKohdealue ammattitaitovaatimuksenkohdealue;

    @OneToMany(mappedBy = "ammattitaitovaatimuksenkohde", cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter
    @Setter
    @OrderColumn(name = "jarjestys")
    private List<Ammattitaitovaatimus> vaatimukset = new ArrayList<>();

    public AmmattitaitovaatimuksenKohde() {
    }

    public AmmattitaitovaatimuksenKohde(AmmattitaitovaatimuksenKohdealue parent, AmmattitaitovaatimuksenKohde other) {
        this.otsikko = other.otsikko;
        this.selite = other.selite;
        this.ammattitaitovaatimuksenkohdealue = parent;

        for (Ammattitaitovaatimus vaatimus : other.vaatimukset) {
            this.vaatimukset.add(new Ammattitaitovaatimus(this, vaatimus));
        }
    }
}
