package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.domain.arviointi.ArviointiAsteikko;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "kvliite")
@Audited
public class KVLiite extends AbstractAuditedEntity implements Serializable, ReferenceableEntity, HistoriaTapahtuma {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @Override
    public Reference getReference() {
        return new Reference(id);
    }

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "peruste_id")
    @Getter
    @Setter
    private Peruste peruste;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pohja_id")
    @Getter
    @Setter
    private KVLiite pohja;

//     Perusteesta:
//     Tutkinnon nimi (fi/sv/en)
//        - kirjoitetaanko vai tuleeko perusteesta?
//        - voimaantulopäivä
//        - diaarinumero
//    Tutkinnossa osoitettu ammatillinen osaaminen
//        - Tutkinnon muodostuminen (sanallinen  kuvaus)

    @ValidHtml(whitelist = ValidHtml.WhitelistType.NORMAL)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen suorittaneenOsaaminen;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.NORMAL)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen tyotehtavatJoissaVoiToimia;

    @Getter
    @Setter
    @Deprecated
    @Column(name = "tutkintotodistuksenAntaja")
    private String tutkintotodistuksenAntajaVanha;


    @ValidHtml(whitelist = ValidHtml.WhitelistType.NORMAL)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen tutkintotodistuksenAntaja;

    @Getter
    @Setter
    @Deprecated
    @Column(name = "tutkinnostaPaattavaViranomainen")
    private String tutkinnostaPaattavaViranomainenVanha;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.NORMAL)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen tutkinnostaPaattavaViranomainen;

    @ManyToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private ArviointiAsteikko arvosanaAsteikko;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen jatkoopintoKelpoisuus;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kansainvalisetSopimukset;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen saadosPerusta;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen tutkintotodistuksenSaaminen;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen pohjakoulutusvaatimukset;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen lisatietoja;

    public TekstiPalanen getTutkintotodistuksenAntaja() {
        if (tutkintotodistuksenAntaja == null) {
            Map<Kieli, String> tekstit = new HashMap<>();
            for (Kieli kieli : Kieli.values()) {
                tekstit.put(kieli, getTutkintotodistuksenAntajaVanha());
            }
            return TekstiPalanen.of(tekstit);
        } else {
            return tutkintotodistuksenAntaja;
        }
    }

    public TekstiPalanen getTutkinnostaPaattavaViranomainen() {
        if (tutkinnostaPaattavaViranomainen == null) {
            Map<Kieli, String> tekstit = new HashMap<>();
            for (Kieli kieli : Kieli.values()) {
                tekstit.put(kieli, getTutkinnostaPaattavaViranomainenVanha());
            }
            return TekstiPalanen.of(tekstit);
        } else {
            return tutkinnostaPaattavaViranomainen;
        }
    }

    @Override
    public TekstiPalanen getNimi() {
        return null;
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.kvliite;
    }
}
