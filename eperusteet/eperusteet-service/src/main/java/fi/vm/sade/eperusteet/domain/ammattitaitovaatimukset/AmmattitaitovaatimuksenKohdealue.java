package fi.vm.sade.eperusteet.domain.ammattitaitovaatimukset;

import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 * Created by autio on 20.10.2015.
 */
@Entity
@Table(name = "ammattitaitovaatimuksenkohdealue")
@Audited
public class AmmattitaitovaatimuksenKohdealue implements Serializable {

    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    private TekstiPalanen otsikko;

    @OneToMany(mappedBy = "ammattitaitovaatimuksenkohdealue", cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter
    @Setter
    private List<AmmattitaitovaatimuksenKohde> vaatimuksenKohteet = new ArrayList<>();

    @Getter
    @NotAudited
    @RelatesToPeruste
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "ammattitaitovaatimuksenkohdealue_tutkinnonosa",
            inverseJoinColumns = @JoinColumn(name = "tutkinnonosa_id"),
            joinColumns = @JoinColumn(name = "ammattitaitovaatimuksenkohdealue_id"))
    private Set<TutkinnonOsa> tutkinnonOsat = new HashSet<>();

    public AmmattitaitovaatimuksenKohdealue() {
    }

    public AmmattitaitovaatimuksenKohdealue(AmmattitaitovaatimuksenKohdealue other) {
        this.otsikko = other.getOtsikko();
        for (AmmattitaitovaatimuksenKohde kohde : other.getVaatimuksenKohteet()) {
            this.vaatimuksenKohteet.add(new AmmattitaitovaatimuksenKohde(this, kohde));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AmmattitaitovaatimuksenKohdealue that = (AmmattitaitovaatimuksenKohdealue) o;

        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }

        return true;
    }

    public void connectAmmattitaitovaatimuksetToKohdealue(AmmattitaitovaatimuksenKohdealue ammattitaitovaatimuksenKohdealue) {
        for (AmmattitaitovaatimuksenKohde ammattitaitovaatimuksenKohde : this.getVaatimuksenKohteet()) {
            ammattitaitovaatimuksenKohde.setAmmattitaitovaatimuksenkohdealue(ammattitaitovaatimuksenKohdealue);
            for (Ammattitaitovaatimus ammattitaitovaatimus : ammattitaitovaatimuksenKohde.getVaatimukset()) {
                ammattitaitovaatimus.setAmmattitaitovaatimuksenkohde(ammattitaitovaatimuksenKohde);
            }
        }
    }
}
