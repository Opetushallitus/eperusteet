package fi.vm.sade.eperusteet.domain.vst;

import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

@Entity
@Table(name = "tavoitesisaltoalue")
@Audited
@Getter
@Setter
public class Tavoitesisaltoalue extends PerusteenOsa implements Serializable {
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Koodi nimiKoodi;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen teksti;

    @OrderColumn
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "tavoitesisaltoalue_tavoitealueet",
            joinColumns = @JoinColumn(name = "tavoitesisaltoalue_id"),
            inverseJoinColumns = @JoinColumn(name = "tavoitealue_id"))
    private List<TavoiteAlue> tavoitealueet = new ArrayList<>();

    public Tavoitesisaltoalue() {

    }

    public Tavoitesisaltoalue(Tavoitesisaltoalue other) {
        copyState(other);
    }

    @Override
    public Tavoitesisaltoalue copy() {
        return new Tavoitesisaltoalue(this);
    }

    @Override
    public Reference getReference() {
        return new Reference(getId());
    }

    @Override
    public void mergeState(PerusteenOsa perusteenOsa) {
        super.mergeState(perusteenOsa);
        if (perusteenOsa instanceof Tavoitesisaltoalue) {
            copyState((Tavoitesisaltoalue) perusteenOsa);
        }
    }

    @Override
    public boolean structureEquals(PerusteenOsa updated) {
        boolean result = false;
        if (updated instanceof Tavoitesisaltoalue) {
            Tavoitesisaltoalue that = (Tavoitesisaltoalue) updated;
            result = super.structureEquals(that);
            result &= getTeksti() == null || refXnor(getTeksti(), that.getTeksti());
            result &= Objects.equals(getNimiKoodi(), that.getNimiKoodi());

            if (result && getTavoitealueet() != null) {
                Iterator<TavoiteAlue> i = getTavoitealueet().iterator();
                Iterator<TavoiteAlue> j = that.getTavoitealueet().iterator();
                while (result && i.hasNext() && j.hasNext()) {
                    result &= Objects.equals(i.next(), j.next());
                }
                result &= !i.hasNext();
                result &= !j.hasNext();
            }

        }
        return result;
    }

    private void copyState(Tavoitesisaltoalue other) {
        if (other == null) {
            return;
        }

        setNimi(other.getNimi());
        setNimiKoodi(other.getNimiKoodi());
        setTeksti(other.getTeksti());

        this.tavoitealueet = new ArrayList<>();
        setTavoitealueet(other.getTavoitealueet());
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.tavoitesisaltoalue;
    }

}
