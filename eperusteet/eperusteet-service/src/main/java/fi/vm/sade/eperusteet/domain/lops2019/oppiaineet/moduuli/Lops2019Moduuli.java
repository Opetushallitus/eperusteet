package fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.moduuli;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.lops2019.Koodillinen;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.Lops2019Oppiaine;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidKoodisto;
import fi.vm.sade.eperusteet.domain.yl.Nimetty;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoUriArvo;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static fi.vm.sade.eperusteet.service.util.Util.identityEquals;
import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

@Entity
@Audited
@Table(name = "yl_lops2019_moduuli")
public class Lops2019Moduuli extends AbstractAuditedReferenceableEntity implements Nimetty, Koodillinen, Copyable<Lops2019Moduuli> {

    @Getter
    @Setter
    @NotNull
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen nimi;

    @Getter
    @Setter
    @ValidHtml(whitelist = ValidHtml.WhitelistType.NORMAL)
    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @Getter
    @Setter
    @NotNull
    private Boolean pakollinen;

    @Getter
    @Setter
    @Column(precision = 10, scale = 2)
    private BigDecimal laajuus;

    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @ValidKoodisto(koodisto = KoodistoUriArvo.MODUULIKOODISTOLOPS2021)
    private Koodi koodi;

    @Getter
    @Setter
    @JoinColumn(name = "tavoitteet_id")
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Lops2019ModuuliTavoite tavoitteet;

    @Getter
    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(name = "yl_lops2019_oppiaine_moduuli_sisalto",
            joinColumns = @JoinColumn(name = "moduuli_id"),
            inverseJoinColumns = @JoinColumn(name = "sisalto_id"))
    @OrderBy("id")
    private List<Lops2019ModuuliSisalto> sisallot = new ArrayList<>();

    @Getter
    @Setter
    private Integer jarjestys;

    @Getter
    @NotAudited
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(name = "yl_lops2019_oppiaine_moduuli",
            joinColumns = {@JoinColumn(name = "moduuli_id", insertable = false, updatable = false)},
            inverseJoinColumns = {@JoinColumn(name = "oppiaine_id", insertable = false, updatable = false)})
    private Lops2019Oppiaine oppiaine; // Moduuli viittaus oppiaineeseen/oppimäärään

    public void asetaOppiaine(Lops2019Oppiaine oa) {
        this.oppiaine = oa;
    }

    public void setSisallot(List<Lops2019ModuuliSisalto> sisallot) {
        this.sisallot.clear();
        if (sisallot != null) {
            this.sisallot.addAll(sisallot);
        }
    }

    @Override
    public Lops2019Moduuli copy(boolean deep) {
        Lops2019Moduuli result = new Lops2019Moduuli();
        result.setPakollinen(this.getPakollinen());
        result.setLaajuus(this.getLaajuus());
//        result.setKoodi(this.getKoodi());
        result.setNimi(TekstiPalanen.of(this.getNimi()));
        result.setKuvaus(TekstiPalanen.of(this.getKuvaus()));

        if (deep) {
            if (this.tavoitteet != null) {
                result.setTavoitteet(this.getTavoitteet().copy());
            }
            if (this.sisallot != null) {
                result.sisallot = this.getSisallot().stream()
                        .map(Copyable::copy)
                        .collect(Collectors.toList());
            }
        }
        return result;
    }

    public boolean structureEquals(Lops2019Moduuli other) {
        boolean result = Objects.equals(this.getId(), other.getId());

        result &= refXnor(this.getNimi(), other.getNimi());
        result &= refXnor(this.getKuvaus(), other.getKuvaus());
        result &= Objects.equals(this.getPakollinen(), other.getPakollinen());
        result &= Objects.equals(this.getLaajuus(), other.getLaajuus());
        result &= Objects.equals(this.getKoodi(), other.getKoodi());
        result &= refXnor(this.getTavoitteet(), other.getTavoitteet());
        result &= refXnor(this.getSisallot(), other.getSisallot());
        result &= identityEquals(this.getOppiaine(), other.getOppiaine());

        if (this.getTavoitteet() != null && other.getTavoitteet() != null) {
            result &= this.getTavoitteet().structureEquals(other.getTavoitteet());
        }

        // sisallot
        if (this.getSisallot() != null && other.getSisallot() != null) {
            result &= this.getSisallot().size() == other.getSisallot().size();
            for (Lops2019ModuuliSisalto s : this.getSisallot()) {
                if (!result) {
                    break;
                }
                for (Lops2019ModuuliSisalto os : other.getSisallot()) {
                    if (Objects.equals(s.getId(), os.getId())) {
                        result &= s.structureEquals(os);
                        break;
                    }
                }
            }
        }

        return result;
    }
}
