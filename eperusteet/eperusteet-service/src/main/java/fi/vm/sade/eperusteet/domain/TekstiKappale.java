package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.domain.liite.Liitteellinen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.dto.Reference;

import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

 @Entity
 @Table(name = "tekstikappale")
 @Audited
 public class TekstiKappale extends PerusteenOsa implements Serializable, Liitteellinen {

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen teksti;

    @ManyToOne(cascade = {CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Koodi osaamisala;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Koodi tutkintonimike;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @JoinColumn(name = "koodi_id")
    private Koodi koodi;

    @Getter
    @Setter
    @NotNull
    private boolean liite = false;

    public TekstiKappale() {
    }

    public TekstiKappale(TekstiKappale other) {
        super(other);
        copyState(other);
    }

    @Override
    public Reference getReference() {
	return new Reference(getId());
    }

    public TekstiPalanen getTeksti() {
        return teksti;
    }

    public void setTeksti(TekstiPalanen teksti) {
        this.teksti = teksti;
    }

    @Override
    public void mergeState(PerusteenOsa perusteenOsa) {
        super.mergeState(perusteenOsa);
        if (perusteenOsa instanceof TekstiKappale) {
            copyState((TekstiKappale) perusteenOsa);
        }
    }

    @Override
    public TekstiKappale copy() {
        return new TekstiKappale(this);
    }

    @Override
    public boolean structureEquals(PerusteenOsa other) {
        boolean result = false;
        if (other instanceof TekstiKappale) {
            TekstiKappale that = (TekstiKappale)other;
            result = super.structureEquals(that);
            result &= getOsaamisala() == null || Objects.equals(getOsaamisala(), that.getOsaamisala());
            result &= getTutkintonimike() == null || Objects.equals(getTutkintonimike(), that.getTutkintonimike());
            result &= getKoodi() == null || Objects.equals(getKoodi(), that.getKoodi());
            // Sallitaan liitetiedon muutos julkaistulle perusteelle
            // result &= Objects.equals(isLiite(), that.isLiite());
            result &= getTeksti() == null || refXnor(getTeksti(), that.getTeksti());
         }
        return result;
    }

    private void copyState(TekstiKappale other) {
        this.setTeksti(other.getTeksti());
        this.setOsaamisala(other.getOsaamisala());
        this.setTutkintonimike(other.getTutkintonimike());
        this.setKoodi(other.getKoodi());
        this.setLiite(other.isLiite());
    }

     @Override
     public NavigationType getNavigationType() {
         return NavigationType.tekstikappale;
     }

     @Override
     public PoistetunTyyppi getPoistetunTyyppi() {
         return PoistetunTyyppi.TEKSTIKAPPALE;
     }
 }
