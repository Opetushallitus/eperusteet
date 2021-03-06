package fi.vm.sade.eperusteet.domain.lops2019.oppiaineet;

import fi.vm.sade.eperusteet.domain.Copyable;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Audited
@Table(name = "yl_lops2019_oppiaine_arviointi")
public class Lops2019Arviointi implements Copyable<Lops2019Arviointi> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.NORMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    public boolean structureEquals(Lops2019Arviointi other) {
        return true;
    }

    @Override
    public Lops2019Arviointi copy(boolean deep) {
        Lops2019Arviointi arviointi = new Lops2019Arviointi();
        arviointi.setKuvaus(TekstiPalanen.of(this.getKuvaus()));
        return arviointi;
    }
}
