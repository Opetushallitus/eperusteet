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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

@Entity
@Table(name = "koto_laaja_alainen_osaaminen")
@Audited
@Getter
@Setter
public class KotoLaajaAlainenOsaaminen extends PerusteenOsa implements Serializable {

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen yleiskuvaus;

    @OrderColumn
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "koto_laaja_alainen_osaaminen_osaamisen_alue",
            joinColumns = @JoinColumn(name = "koto_laaja_alainen_osaaminen_id"),
            inverseJoinColumns = @JoinColumn(name = "osaamisalue_id"))
    private List<KotoLaajaAlaisenOsaamisenAlue> osaamisAlueet = new ArrayList<>();


    public KotoLaajaAlainenOsaaminen() {
    }

    public KotoLaajaAlainenOsaaminen(KotoLaajaAlainenOsaaminen other) {
        super(other);
        copyState(other);
    }

    @Override
    public void mergeState(PerusteenOsa perusteenOsa) {
        super.mergeState(perusteenOsa);
        if (perusteenOsa instanceof KotoLaajaAlainenOsaaminen) {
            KotoLaajaAlainenOsaaminen other = (KotoLaajaAlainenOsaaminen) perusteenOsa;
            setNimi(other.getNimi());
            setYleiskuvaus(other.getYleiskuvaus());
            this.osaamisAlueet = new ArrayList<>(other.osaamisAlueet);
        }
    }

    private void copyState(KotoLaajaAlainenOsaaminen other) {
        if (other == null) {
            return;
        }

        this.yleiskuvaus = other.getYleiskuvaus();
        this.osaamisAlueet = other.getOsaamisAlueet().stream().map(KotoLaajaAlaisenOsaamisenAlue::new).collect(Collectors.toList());
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

    @Override
    public boolean structureEquals(PerusteenOsa updated) {
        KotoLaajaAlainenOsaaminen updatedKoto = (KotoLaajaAlainenOsaaminen) updated;
        boolean result = super.structureEquals(updatedKoto);
        result &= getYleiskuvaus() == null || refXnor(getYleiskuvaus(), updatedKoto.getYleiskuvaus());

        return result;
    }
}
