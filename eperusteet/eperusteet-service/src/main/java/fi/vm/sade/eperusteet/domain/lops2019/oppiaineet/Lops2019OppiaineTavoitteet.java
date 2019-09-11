package fi.vm.sade.eperusteet.domain.lops2019.oppiaineet;

import fi.vm.sade.eperusteet.domain.Copyable;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

@Entity
@Getter
@Setter
@Audited
@Table(name = "yl_lops2019_oppiaine_tavoitteet")
public class Lops2019OppiaineTavoitteet implements Copyable<Lops2019OppiaineTavoitteet> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.NORMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @OrderColumn
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinTable(name = "yl_lops2019_oppiaine_tavoitteet_tavoitealue",
            joinColumns = @JoinColumn(name = "tavoitteet_id"),
            inverseJoinColumns = @JoinColumn(name = "tavoitealue_id"))
    private List<Lops2019OppiaineTavoitealue> tavoitealueet = new ArrayList<>();

    public boolean structureEquals(Lops2019OppiaineTavoitteet other) {
        boolean result = refXnor(this.getTavoitealueet(), other.getTavoitealueet());

        if (this.getTavoitealueet() != null && other.getTavoitealueet() != null) {
            result &= this.getTavoitealueet().size() == other.getTavoitealueet().size();
            for (Lops2019OppiaineTavoitealue ta : this.getTavoitealueet()) {
                if (!result) {
                    break;
                }
                for (Lops2019OppiaineTavoitealue ota : other.getTavoitealueet()) {
                    if (Objects.equals(ta.getId(), ota.getId())) {
                        result &= ta.structureEquals(ota);
                    }
                }
            }
        }

        return result;
    }

    @Override
    public Lops2019OppiaineTavoitteet copy(boolean deep) {
        Lops2019OppiaineTavoitteet tavoitteet = new Lops2019OppiaineTavoitteet();
        tavoitteet.setKuvaus(TekstiPalanen.of(this.getKuvaus()));
        if (deep) {
            if (this.getTavoitealueet() != null) {
                tavoitteet.setTavoitealueet(this.getTavoitealueet().stream()
                .map(Copyable::copy)
                .collect(Collectors.toList()));
            }
        }
        return tavoitteet;
    }
}
