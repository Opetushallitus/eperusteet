package fi.vm.sade.eperusteet.domain.lops2019.oppiaineet;

import fi.vm.sade.eperusteet.domain.Copyable;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import jakarta.persistence.*;
import java.util.Objects;

@Getter
@Setter
@Entity
@Audited
@Table(name = "yl_lops2019_oppiaine_tehtava")
public class Lops2019Tehtava implements Copyable<Lops2019Tehtava> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.NORMAL)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    public boolean structureEquals(Lops2019Tehtava other) {
        return Objects.equals(this.getId(), other.getId());
    }

    @Override
    public Lops2019Tehtava copy(boolean deep) {
        Lops2019Tehtava tehtava = new Lops2019Tehtava();
        tehtava.setKuvaus(TekstiPalanen.of(this.getKuvaus()));
        return tehtava;
    }
}
