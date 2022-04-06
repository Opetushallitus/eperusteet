package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.domain.validation.ValidKoodisto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoUriArvo;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Audited
@Getter
@Setter
@Table(name = "opas_kiinnitetty_koodi")
public class OppaanKiinnitettyKoodi extends AbstractAuditedReferenceableEntity {

    @ManyToOne
    @NotNull
    private OpasSisalto opasSisalto;

    @Enumerated(EnumType.STRING)
    @NotNull
    private KiinnitettyKoodiTyyppi kiinnitettyKoodiTyyppi;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidKoodisto(koodisto = {KoodistoUriArvo.TUTKINNONOSAT, KoodistoUriArvo.OSAAMISALA, KoodistoUriArvo.OPPIAINEETJAOPPIMAARATLOPS2021, KoodistoUriArvo.OPINTOKOKONAISUUSNIMET, KoodistoUriArvo.KOULUTUKSENOSATTUVA})
    private Koodi koodi;

    public OppaanKiinnitettyKoodi copy(OpasSisalto opasSisalto) {
        OppaanKiinnitettyKoodi oppaanKiinnitettyKoodi = new OppaanKiinnitettyKoodi();
        oppaanKiinnitettyKoodi.setOpasSisalto(opasSisalto);
        oppaanKiinnitettyKoodi.setKiinnitettyKoodiTyyppi(kiinnitettyKoodiTyyppi);
        oppaanKiinnitettyKoodi.setKoodi(koodi);
        return oppaanKiinnitettyKoodi;
    }
}
