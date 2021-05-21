package fi.vm.sade.eperusteet.domain.tuva;

import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.liite.Liitteellinen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidKoodisto;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoUriArvo;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

@Entity
@Table(name = "tuva_laajaalainenosaaminen")
@Audited
@Getter
@Setter
public class TuvaLaajaAlainenOsaaminen extends PerusteenOsa implements Serializable, Liitteellinen {

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidKoodisto(koodisto = KoodistoUriArvo.TUTKINTOKOULUTUKSEEN_VALMENTAVAKOULUTUS_LAAJAALAINENOSAAMINEN)
    private Koodi nimiKoodi;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen teksti;

    @Getter
    @Setter
    @NotNull
    private boolean liite = false;

    public TuvaLaajaAlainenOsaaminen() {

    }

    public TuvaLaajaAlainenOsaaminen(TuvaLaajaAlainenOsaaminen other) {
        copyState(other);
    }

    @Override
    public TuvaLaajaAlainenOsaaminen copy() {
        return new TuvaLaajaAlainenOsaaminen(this);
    }

    @Override
    public Reference getReference() {
        return new Reference(getId());
    }

    @Override
    public void mergeState(PerusteenOsa perusteenOsa) {
        super.mergeState(perusteenOsa);
        if (perusteenOsa instanceof TuvaLaajaAlainenOsaaminen) {
            copyState((TuvaLaajaAlainenOsaaminen) perusteenOsa);
        }
    }

    @Override
    public boolean structureEquals(PerusteenOsa updated) {
        boolean result = false;
        if (updated instanceof TuvaLaajaAlainenOsaaminen) {
            TuvaLaajaAlainenOsaaminen that = (TuvaLaajaAlainenOsaaminen) updated;
            result = super.structureEquals(that);
            result &= Objects.equals(getNimiKoodi(), that.getNimiKoodi());
            result &= Objects.equals(getNimi(), that.getNimi());
            result &= getTeksti() == null || refXnor(getTeksti(), that.getTeksti());
        }
        return result;
    }

    private void copyState(TuvaLaajaAlainenOsaaminen other) {
        if (other == null) {
            return;
        }

        setNimiKoodi(other.getNimiKoodi());
        setNimi(other.getNimi());
        setTeksti(other.getTeksti());
        setLiite(other.isLiite());
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.laajaalainenosaaminen;
    }
}
