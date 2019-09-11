package fi.vm.sade.eperusteet.domain.lops2019.oppiaineet;

import fi.vm.sade.eperusteet.domain.Copyable;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

@Entity
@Audited
@Table(name = "yl_lops2019_oppiaine_tavoitealue")
public class Lops2019OppiaineTavoitealue implements Copyable<Lops2019OppiaineTavoitealue> {

    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Getter
    @Setter
    @NotNull
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen nimi;

    @Getter
    @Setter
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kohde;

    @Getter
    @Setter
    @OrderColumn
    @BatchSize(size = 25)
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(name = "yl_lops2019_oppiaine_tavoitealue_tekstipalanen",
            joinColumns = @JoinColumn(name = "tavoitealue_id"),
            inverseJoinColumns = @JoinColumn(name = "tekstipalanen_id"))
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private List<TekstiPalanen> tavoitteet = new ArrayList<>();

    public boolean structureEquals(Lops2019OppiaineTavoitealue other) {
        boolean result = refXnor(this.getTavoitteet(), other.getTavoitteet());

        if (this.getTavoitteet() != null && other.getTavoitteet() != null) {
            result &= this.getTavoitteet().size() == other.getTavoitteet().size();
            for (TekstiPalanen t : this.getTavoitteet()) {
                if (!result) {
                    break;
                }
                for (TekstiPalanen ot : other.getTavoitteet()) {
                    if (t.getId().equals(ot.getId())) {
                        result &= t.equals(ot);
                    }
                }
            }
        }

        return result;
    }

    @Override
    public Lops2019OppiaineTavoitealue copy(boolean deep) {
        Lops2019OppiaineTavoitealue tavoitealue = new Lops2019OppiaineTavoitealue();
        tavoitealue.setNimi(TekstiPalanen.of(this.getNimi()));
        tavoitealue.setKohde(TekstiPalanen.of(this.getKohde()));
        if (deep) {
            if (this.getTavoitteet() != null) {
                tavoitealue.setTavoitteet(this.getTavoitteet().stream()
                        .map(TekstiPalanen::of)
                        .collect(Collectors.toList()));
            }
        }
        return tavoitealue;
    }
}
