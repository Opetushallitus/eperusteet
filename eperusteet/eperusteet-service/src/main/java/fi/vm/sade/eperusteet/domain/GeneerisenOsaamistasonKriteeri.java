package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Audited
@Table(name = "geneerisenosaamistasonkriteeri")
public class GeneerisenOsaamistasonKriteeri extends AbstractAuditedReferenceableEntity implements Copyable<GeneerisenOsaamistasonKriteeri> {

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Osaamistaso osaamistaso;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @OrderColumn
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    private List<TekstiPalanen> kriteerit = new ArrayList<>();

    @Override
    public GeneerisenOsaamistasonKriteeri copy(boolean deep) {
        GeneerisenOsaamistasonKriteeri uusi = new GeneerisenOsaamistasonKriteeri();
        uusi.setOsaamistaso(this.getOsaamistaso());
        uusi.setKriteerit(new ArrayList<>(getKriteerit()));
        return uusi;
    }
}
