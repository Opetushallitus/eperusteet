package fi.vm.sade.eperusteet.domain.lops2019.oppiaineet;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.lops2019.Koodillinen;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.moduuli.Lops2019Moduuli;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidKoodisto;
import fi.vm.sade.eperusteet.domain.yl.Nimetty;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoUriArvo;
import fi.vm.sade.eperusteet.service.util.PerusteUtils;
import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

@Entity
@Audited
@Table(name = "yl_lops2019_oppiaine")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Lops2019Oppiaine extends AbstractAuditedReferenceableEntity implements
        Koodillinen,
        StructurallyComparable<Lops2019Oppiaine>,
        Nimetty,
        Copyable<Lops2019Oppiaine> {

    @Getter
    @Setter
    @NotNull
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen nimi;

    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @ValidKoodisto(koodisto = KoodistoUriArvo.OPPIAINEETJAOPPIMAARATLOPS2021)
    private Koodi koodi;

    @Getter
    @Setter
    @JoinColumn(name = "pakolliset_moduulit_kuvaus_id")
    @ValidHtml(whitelist = ValidHtml.WhitelistType.NORMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen pakollisetModuulitKuvaus;

    @Getter
    @Setter
    @JoinColumn(name = "valinnaiset_moduulit_kuvaus_id")
    @ValidHtml(whitelist = ValidHtml.WhitelistType.NORMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen valinnaisetModuulitKuvaus;


    @Getter
    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(name = "yl_lops2019_oppiaine_moduuli",
            joinColumns = @JoinColumn(name = "oppiaine_id"),
            inverseJoinColumns = @JoinColumn(name = "moduuli_id"))
    @OrderBy("jarjestys, id")
    private List<Lops2019Moduuli> moduulit = new ArrayList<>();

    @Getter
    @Setter
    @JoinColumn(name="arviointi_id")
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Lops2019Arviointi arviointi;

    @Getter
    @Setter
    @JoinColumn(name="tehtava_id")
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Lops2019Tehtava tehtava;

    @Getter
    @Setter
    @JoinColumn(name="lao_id")
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Lops2019OppiaineLaajaAlainenOsaaminen laajaAlaisetOsaamiset;

    @Getter
    @Setter
    @JoinColumn(name = "tavoitteet_id")
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Lops2019OppiaineTavoitteet tavoitteet;

    @Getter
    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(name = "yl_lops2019_oppiaine_oppimaara",
            joinColumns = @JoinColumn(name = "oppiaine_id"),
            inverseJoinColumns = @JoinColumn(name = "oppimaara_id"))
    @OrderBy("jarjestys, id")
    private List<Lops2019Oppiaine> oppimaarat = new ArrayList<>();

    @Getter
    @Setter
    private Integer jarjestys;

    @Getter
    @NotAudited
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(name = "yl_lops2019_oppiaine_oppimaara",
            joinColumns = {@JoinColumn(name = "oppimaara_id", insertable = false, updatable = false)},
            inverseJoinColumns = {@JoinColumn(name = "oppiaine_id", insertable = false, updatable = false)})
    private Lops2019Oppiaine oppiaine; // Oppimäärän viittaus oppiaineeseen

    public void asetaOppiaine(Lops2019Oppiaine oppiaine) {
        this.oppiaine = oppiaine;
    }

    public void setOppimaarat(final List<Lops2019Oppiaine> oppimaarat) {
        if (this.oppimaarat == null) {
            this.oppimaarat = new ArrayList<>();
        }
        this.oppimaarat.clear();
        this.oppimaarat.addAll(oppimaarat);
    }

    public void setModuulit(final List<Lops2019Moduuli> moduulit) {
        if (this.moduulit == null) {
            this.moduulit = new ArrayList<>();
        }
        this.moduulit.clear();
        this.moduulit.addAll(moduulit);
    }

    @Override
    public boolean structureEquals(final Lops2019Oppiaine other) {
        boolean result = true;
        result &= refXnor(this.getNimi(), other.getNimi());
        result &= Objects.equals(this.getKoodi(), other.getKoodi());
        result &= refXnor(this.getModuulit(), other.getModuulit());
        result &= refXnor(this.getTavoitteet(), other.getTavoitteet());
        result &= refXnor(this.getOppimaarat(), other.getOppimaarat());
        result &= PerusteUtils.nestedStructureEquals(this.getModuulit(), other.getModuulit());
        result &= PerusteUtils.nestedStructureEquals(this.getOppimaarat(), other.getOppimaarat());

        // tavoitteet
        if (this.getTavoitteet() != null && other.getTavoitteet() != null) {
            result &= this.getTavoitteet().structureEquals(other.getTavoitteet());
        }

        return result;
    }

    @Override
    public Lops2019Oppiaine copy(final boolean deep) {
        final Lops2019Oppiaine result = new Lops2019Oppiaine();
        result.setNimi(TekstiPalanen.of(this.getNimi()));
        result.setKoodi(this.getKoodi());

        if (this.getArviointi() != null) {
            result.setArviointi(this.getArviointi().copy());
        }
        if (this.getTehtava() != null) {
            result.setTehtava(this.getTehtava().copy());
        }
        if (this.getLaajaAlaisetOsaamiset() != null) {
            result.setLaajaAlaisetOsaamiset(this.getLaajaAlaisetOsaamiset().copy());
        }
        if (this.getTavoitteet() != null) {
            result.setTavoitteet(this.getTavoitteet().copy());
        }

        if (deep) {
            if (this.moduulit != null) {
                result.setModuulit(this.getModuulit().stream()
                        .map(Copyable::copy)
                        .collect(Collectors.toList()));
            }
            if (this.getOppimaarat() != null) {
                result.setOppimaarat(this.getOppimaarat().stream()
                        .map(Copyable::copy)
                        .collect(Collectors.toList()));
            }
        }

        return result;
    }

}
