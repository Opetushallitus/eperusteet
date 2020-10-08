package fi.vm.sade.eperusteet.domain.tutkinnonosa;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.util.*;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

@Entity
@Table(name = "ammattitaitovaatimukset2019")
@Audited
@NoArgsConstructor
public class Ammattitaitovaatimukset2019 extends AbstractAuditedReferenceableEntity {

    @ValidHtml
    @Getter
    @Setter
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kohde;

    @Getter
    @Setter
    @OrderColumn(name = "jarjestys")
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "ammattitaitovaatimukset2019_ammattitaitovaatimus",
            joinColumns = @JoinColumn(name = "ammattitaitovaatimukset_id"),
            inverseJoinColumns = @JoinColumn(name = "ammattitaitovaatimus_id"))
    private List<Ammattitaitovaatimus2019> vaatimukset = new ArrayList<>();

    @Getter
    @Setter
    @OrderColumn(name = "jarjestys")
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "ammattitaitovaatimukset2019_kohdealue",
            joinColumns = @JoinColumn(name = "ammattitaitovaatimukset_id"),
            inverseJoinColumns = @JoinColumn(name = "kohdealue_id"))
    private List<Ammattitaitovaatimus2019Kohdealue> kohdealueet = new ArrayList<>();

    public Ammattitaitovaatimukset2019(Ammattitaitovaatimukset2019 other) {
        this.kohde = other.kohde;

        if (other.getVaatimukset() != null) {
            this.vaatimukset = new ArrayList<>();
            for (Ammattitaitovaatimus2019 vaatimus : other.getVaatimukset()) {
                this.vaatimukset.add(Ammattitaitovaatimus2019.of(vaatimus.getVaatimus()));
            }
        }

        if (other.getKohdealueet() != null) {
            this.kohdealueet = new ArrayList<>();
            for (Ammattitaitovaatimus2019Kohdealue kohde : other.getKohdealueet()) {
                this.kohdealueet.add(new Ammattitaitovaatimus2019Kohdealue(kohde));
            }
        }
    }

    public boolean structureEquals(Ammattitaitovaatimukset2019 other) {
        if (this == other) {
            return true;
        }
        boolean result = refXnor(getKohde(), other.getKohde());

        if (result && getVaatimukset() != null) {
            Iterator<Ammattitaitovaatimus2019> i = getVaatimukset().iterator();
            Iterator<Ammattitaitovaatimus2019> j = other.getVaatimukset().iterator();
            while (result && i.hasNext() && j.hasNext()) {
                result &= i.next().structureEquals(j.next());
            }
            result &= !i.hasNext();
            result &= !j.hasNext();
        }

        if (result && getKohdealueet() != null) {
            Iterator<Ammattitaitovaatimus2019Kohdealue> i = getKohdealueet().iterator();
            Iterator<Ammattitaitovaatimus2019Kohdealue> j = other.getKohdealueet().iterator();
            while (result && i.hasNext() && j.hasNext()) {
                result &= i.next().structureEquals(j.next());
            }
            result &= !i.hasNext();
            result &= !j.hasNext();
        }

        return result;
    }
}
