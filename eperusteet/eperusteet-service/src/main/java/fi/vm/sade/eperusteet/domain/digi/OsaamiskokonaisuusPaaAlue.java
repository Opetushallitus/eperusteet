package fi.vm.sade.eperusteet.domain.digi;

import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "osaamiskokonaisuus_paa_alue")
@Audited
@Getter
@Setter
@NoArgsConstructor
public class OsaamiskokonaisuusPaaAlue extends PerusteenOsa {

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "osaamiskokonaisuus_paa_alue_osa_alueet_join",
            joinColumns = @JoinColumn(name = "osaamiskokonaisuus_paa_alue_id"),
            inverseJoinColumns = @JoinColumn(name = "osaAlueet_id"))
    @OrderColumn
    private List<OsaamiskokonaisuusOsaAlue> osaAlueet = new ArrayList<>();

    @Temporal(TemporalType.TIMESTAMP)
    private Date muokattu;

    public OsaamiskokonaisuusPaaAlue(OsaamiskokonaisuusPaaAlue other) {
        super(other);
        copyState(other);
    }

    public void setOsaAlueet(List<OsaamiskokonaisuusOsaAlue> osaAlueet) {
        this.osaAlueet.clear();
        this.osaAlueet.addAll(osaAlueet);
    }

    @Override
    public void mergeState(PerusteenOsa perusteenOsa) {
        super.mergeState(perusteenOsa);
        if (perusteenOsa instanceof OsaamiskokonaisuusPaaAlue) {
            OsaamiskokonaisuusPaaAlue other = (OsaamiskokonaisuusPaaAlue) perusteenOsa;
            setNimi(other.getNimi());
            setKuvaus(other.getKuvaus());
            setOsaAlueet(other.getOsaAlueet());
            preupdate();
        }
    }

    private void copyState(OsaamiskokonaisuusPaaAlue other) {
        if (other == null) {
            return;
        }

        this.kuvaus = other.getKuvaus();
        osaAlueet = other.getOsaAlueet().stream().map(OsaamiskokonaisuusOsaAlue::new).collect(Collectors.toList());
    }

    @Override
    public PerusteenOsa copy() {
        return new OsaamiskokonaisuusPaaAlue(this);
    }

    @Override
    public Reference getReference() {
        return new Reference(getId());
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.osaamiskokonaisuus_paa_alue;
    }

    @Override
    public boolean structureEquals(PerusteenOsa updated) {
        return true;
    }

    @PreUpdate
    protected void preupdate() {
        muokattu = new Date();
    }
}
