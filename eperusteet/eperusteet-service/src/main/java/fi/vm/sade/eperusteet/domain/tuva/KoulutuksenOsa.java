package fi.vm.sade.eperusteet.domain.tuva;

import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.KoulutusOsanTyyppi;
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
import java.util.stream.Collectors;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

@Entity
@Table(name = "koulutuksenosa")
@Audited
@Getter
@Setter
public class KoulutuksenOsa extends PerusteenOsa implements Serializable {

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Koodi nimiKoodi;

    private Integer laajuusMinimi;

    private Integer laajuusMaksimi;

    @Enumerated(EnumType.STRING)
    private KoulutusOsanKoulutustyyppi koulutusOsanKoulutustyyppi;

    @Enumerated(EnumType.STRING)
    private KoulutusOsanTyyppi koulutusOsanTyyppi;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @Deprecated
    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen tavoitteenKuvaus;

    @OrderColumn
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinTable(name = "koulutuksenosa_tavoitteet",
            joinColumns = @JoinColumn(name = "koulutuksenosa_id"),
            inverseJoinColumns = @JoinColumn(name = "tavoite_id"))
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private List<TekstiPalanen> tavoitteet = new ArrayList<>();

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen keskeinenSisalto;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen laajaAlaisenOsaamisenKuvaus;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen arvioinninKuvaus;

    @Deprecated
    @OrderColumn
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinTable(name = "koulutuksenosa_arvioinnit",
            joinColumns = @JoinColumn(name = "koulutuksenosa_id"),
            inverseJoinColumns = @JoinColumn(name = "arviointi_id"))
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private List<TekstiPalanen> arvioinnit = new ArrayList<>();

    @Deprecated
    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen osaamisenArvioinnista;

    public KoulutuksenOsa() {

    }

    public KoulutuksenOsa(KoulutuksenOsa other) {
        super(other);
        copyState(other);
    }

    @Override
    public KoulutuksenOsa copy() {
        return new KoulutuksenOsa(this);
    }

    @Override
    public Reference getReference() {
        return new Reference(getId());
    }

    @Override
    public void mergeState(PerusteenOsa perusteenOsa) {
        super.mergeState(perusteenOsa);
        if (perusteenOsa instanceof KoulutuksenOsa) {
            KoulutuksenOsa other = (KoulutuksenOsa) perusteenOsa;
            setNimiKoodi(other.getNimiKoodi());
            setNimi(other.getNimi());
            setKuvaus(other.getKuvaus());
            setKoulutusOsanKoulutustyyppi(other.getKoulutusOsanKoulutustyyppi());
            setKoulutusOsanTyyppi(other.getKoulutusOsanTyyppi());
            setLaajuusMinimi(other.getLaajuusMinimi());
            setLaajuusMaksimi(other.getLaajuusMaksimi());
            setKeskeinenSisalto(other.getKeskeinenSisalto());
            setLaajaAlaisenOsaamisenKuvaus(other.getLaajaAlaisenOsaamisenKuvaus());
            setOsaamisenArvioinnista(other.getOsaamisenArvioinnista());
            setArvioinninKuvaus(other.getArvioinninKuvaus());
            setArvioinnit(other.getArvioinnit());
            setTavoitteenKuvaus(other.getTavoitteenKuvaus());
            setTavoitteet(other.getTavoitteet());
        }
    }

    @Override
    public boolean structureEquals(PerusteenOsa updated) {
        boolean result = false;
        if (updated instanceof KoulutuksenOsa) {
            KoulutuksenOsa that = (KoulutuksenOsa) updated;
            result = super.structureEquals(that);
            result &= Objects.equals(getNimiKoodi(), that.getNimiKoodi());
            result &= Objects.equals(getNimi(), that.getNimi());
            result &= Objects.equals(getLaajuusMinimi(), that.getLaajuusMinimi());
            result &= Objects.equals(getLaajuusMaksimi(), that.getLaajuusMaksimi());
            result &= refXnor(getKuvaus(), that.getKuvaus());
            result &= refXnor(getKeskeinenSisalto(), that.getKeskeinenSisalto());
            result &= refXnor(getOsaamisenArvioinnista(), that.getOsaamisenArvioinnista());
            result &= refXnor(getLaajaAlaisenOsaamisenKuvaus(), that.getLaajaAlaisenOsaamisenKuvaus());
            result &= refXnor(getArvioinninKuvaus(), that.getArvioinninKuvaus());

            if (result && getTavoitteet() != null) {
                Iterator<TekstiPalanen> i = getTavoitteet().iterator();
                Iterator<TekstiPalanen> j = that.getTavoitteet().iterator();
                while (result && i.hasNext() && j.hasNext()) {
                    result &= Objects.equals(i.next(), j.next());
                }
                result &= !i.hasNext();
                result &= !j.hasNext();
            }
        }
        return result;
    }

    private void copyState(KoulutuksenOsa other) {
        if (other == null) {
            return;
        }

        this.nimiKoodi = other.getNimiKoodi();
        this.kuvaus = other.getKuvaus();
        this.koulutusOsanKoulutustyyppi = other.getKoulutusOsanKoulutustyyppi();
        this.koulutusOsanTyyppi = other.getKoulutusOsanTyyppi();
        this.laajuusMinimi = other.getLaajuusMinimi();
        this.laajuusMaksimi = other.getLaajuusMaksimi();
        this.keskeinenSisalto = other.getKeskeinenSisalto();
        this.laajaAlaisenOsaamisenKuvaus = other.getLaajaAlaisenOsaamisenKuvaus();
        this.osaamisenArvioinnista = other.getOsaamisenArvioinnista();
        this.arvioinninKuvaus = other.getArvioinninKuvaus();
        this.arvioinnit = other.getArvioinnit().stream().map(TekstiPalanen::of).collect(Collectors.toList());
        this.tavoitteenKuvaus = other.getTavoitteenKuvaus();
        this.tavoitteet = other.getTavoitteet().stream().map(TekstiPalanen::of).collect(Collectors.toList());
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.koulutuksenosa;
    }
}
