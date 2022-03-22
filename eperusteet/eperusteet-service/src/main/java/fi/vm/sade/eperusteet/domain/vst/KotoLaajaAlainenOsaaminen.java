package fi.vm.sade.eperusteet.domain.vst;

import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "koto_laajaalainenosaaminen")
@Audited
@Getter
@Setter
public class KotoLaajaAlainenOsaaminen extends PerusteenOsa implements Serializable {

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen yleiskuvaus;


    public KotoLaajaAlainenOsaaminen() {

    }

    public KotoLaajaAlainenOsaaminen(KotoLaajaAlainenOsaaminen other) {
        copyState(other);
    }

    @Override
    public void mergeState(PerusteenOsa perusteenOsa) {
        super.mergeState(perusteenOsa);
        copyState((KotoLaajaAlainenOsaaminen) perusteenOsa);
    }

    private void copyState(KotoLaajaAlainenOsaaminen other) {
        if (other == null) {
            return;
        }

        setNimi(other.getNimi());
        setYleiskuvaus(other.getYleiskuvaus());
    }

    @Override
    public PerusteenOsa copy() {
        return new KotoLaajaAlainenOsaaminen(this);
    }

    @Override
    public Reference getReference() {
        return new Reference(getId());
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.koto_laajaalainenosaaminen;
    }
}
