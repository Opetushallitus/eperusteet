package fi.vm.sade.eperusteet.domain.tutkinnonrakenne;

import fi.vm.sade.eperusteet.domain.HistoriaTapahtuma;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.Mergeable;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;

import fi.vm.sade.eperusteet.domain.validation.ValidKoodisto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoUriArvo;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.*;
import jakarta.persistence.*;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.springframework.validation.annotation.Validated;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@Validated
@Entity
@DiscriminatorValue("RM")
@Audited
@RakenneModuuli.ValidRakenneModuuli
public class RakenneModuuli extends AbstractRakenneOsa implements Mergeable<RakenneModuuli>, HistoriaTapahtuma {

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Getter
    @Setter
    @Audited(targetAuditMode = NOT_AUDITED)
    private TekstiPalanen nimi;

    @Getter
    @Setter
    private MuodostumisSaanto muodostumisSaanto;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private RakenneModuuliRooli rooli;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private RakenneModuuliErikoisuus erikoisuus;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidKoodisto(koodisto = KoodistoUriArvo.OSAAMISALA)
    private Koodi osaamisala;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidKoodisto(koodisto = KoodistoUriArvo.TUTKINTONIMIKKEET)
    private Koodi tutkintonimike;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinTable(name = "rakennemoduuli_rakenneosa",
               joinColumns = {
                   @JoinColumn(name = "rakennemoduuli_id")},
               inverseJoinColumns = {
                   @JoinColumn(name = "rakenneosa_id")})
    @OrderColumn(name = "osat_order")
    @Getter
    @BatchSize(size = 25)
    private List<AbstractRakenneOsa> osat = new ArrayList<>();

    public void setOsat(List<AbstractRakenneOsa> osat) {
        this.osat.clear();
        if (osat != null) {
            this.osat.addAll(osat);
        }
    }

    @Override
    public void mergeState(RakenneModuuli moduuli) {
        if (moduuli != null) {
            if (moduuli.getOsat() != null & !moduuli.getOsat().isEmpty()) {
                this.rooli = moduuli.getRooli();
            }

            this.setOsat(moduuli.getOsat());
            this.setNimi(moduuli.getNimi());
            this.setKuvaus(moduuli.getKuvaus());
            this.setErikoisuus(moduuli.getErikoisuus());
            this.muodostumisSaanto = moduuli.getMuodostumisSaanto() == null ? null : new MuodostumisSaanto(moduuli.getMuodostumisSaanto());

            if (moduuli.getOsaamisala() != null) {
                this.osaamisala = moduuli.getOsaamisala();
                this.tutkintonimike = null;
                this.rooli = RakenneModuuliRooli.OSAAMISALA;
            }
            else if (moduuli.getTutkintonimike() != null) {
                this.tutkintonimike = moduuli.getTutkintonimike();
                this.osaamisala = null;
                this.rooli = RakenneModuuliRooli.TUTKINTONIMIKE;
            }
        }

    }

    @Override
    public Optional<RakenneOsaVirhe> isSame(AbstractRakenneOsa moduuli, int depth, boolean includeText) {
        if (moduuli == null) {
            return fail("osaa-ei-voi-tyhjentaa");
        }
        else if (moduuli instanceof RakenneModuuli) {
            return isSame((RakenneModuuli) moduuli, depth, includeText);
        }
        else {
            return success();
        }
    }

    public Optional<RakenneOsaVirhe> isSame(RakenneModuuli vanha, int depth, boolean includeText) {
        Optional<RakenneOsaVirhe> isSuperValid = super.isSame(vanha, depth, includeText);
        if (isSuperValid.isPresent()) {
            return isSuperValid;
        }

        if (includeText && !Objects.equals(this.nimi, vanha.getNimi())) {
            return fail("ryhman-nimea-ei-voi-muuttaa");
        }

        if ((this.osat == null && vanha.getOsat() != null) || (this.osat != null && vanha.getOsat() == null)) {
            return fail("ryhman-osia-ei-voi-muuttaa");
        }

        if (!this.getPakollinen().equals(vanha.getPakollinen())) {
            return fail("ryhman-pakollisuutta-ei-voi-muuttaa");
        }

        if (depth > 0) {
            if (this.rooli != vanha.getRooli() && this.rooli != RakenneModuuliRooli.TUTKINTONIMIKE) {
                return fail("ryhman-roolia-ei-voi-vaihtaa");
            }
        }
        else {
            if (this.rooli != null && this.rooli != RakenneModuuliRooli.NORMAALI) {
                return fail("rakenteen-roolia-ei-voi-vaihtaa");
            }
        }


        if (vanha.getTutkintonimike() != null && vanha.getOsaamisala() != null) {
            return fail("ryhman-tutkintonimike-ja-osaamisala-ei-voi-olla-samanaikaisesti");
        }

        if (includeText && osaamisala != null && !Objects.equals(osaamisala, vanha.getOsaamisala())) {
            return fail("ryhman-osaamisalaa-ei-voi-muuttaa");
        }

        if  (includeText && tutkintonimike != null && !Objects.equals(tutkintonimike, vanha.getTutkintonimike())) {
            return fail("ryhman-tutkintonimiketta-ei-voi-vaihtaa");
        }

        if (!Objects.equals(this.getErikoisuus(), vanha.getErikoisuus())) {
            return fail("ryhman-erikoisuustietoa-ei-voi-vaihtaa");
        }

        boolean muodostuminenMuuttunut = !Objects.equals(this.muodostumisSaanto, vanha.getMuodostumisSaanto());
        boolean vanhaMuodostamissaantoMinimi = vanha.getMuodostumisSaanto() != null
                && vanha.getMuodostumisSaanto().getLaajuus() != null
                && vanha.getMuodostumisSaanto().getLaajuus().getMinimi() != null;

        if (depth == 0 && this.muodostumisSaanto != null && muodostuminenMuuttunut && (includeText || vanhaMuodostamissaantoMinimi)) {
            return fail("ryhman-juuren-muodostumista-ei-voi-muuttaa");
        }

        if (depth > 0 && muodostuminenMuuttunut && (includeText || vanhaMuodostamissaantoMinimi)) {
            return fail("ryhman-muodostumissaantoa-ei-voi-muuttaa");
        }

        if (this.osat != null && vanha.getOsat() != null) {
            if (this.rooli == RakenneModuuliRooli.VIRTUAALINEN && !this.getOsat().isEmpty()) {
                return fail("ryhman-rooli-ei-salli-sisaltoa");
            }

            if (this.osat.size() != vanha.getOsat().size()) {
                return fail("ryhman-osien-maaraa-ei-voi-muuttaa");
            }

            Iterator<AbstractRakenneOsa> l = this.getOsat().iterator();
            Iterator<AbstractRakenneOsa> r = vanha.getOsat().iterator();
            while (l.hasNext() && r.hasNext()) {
                AbstractRakenneOsa a = l.next();
                AbstractRakenneOsa b = r.next();
                Optional<RakenneOsaVirhe> same = a.isSame(b, depth + 1, includeText);
                if (same.isPresent()) {
                    return same;
                }
            }
        }

        return success();
    }

    public boolean isInRakenne(TutkinnonOsaViite viite, boolean ylinTaso) {
        for (AbstractRakenneOsa rakenneosa : osat) {
            if (rakenneosa instanceof RakenneModuuli) {
                if (((RakenneModuuli) rakenneosa).isInRakenne(viite, false)) {
                    return true;
                }
            } else if (rakenneosa instanceof RakenneOsa) {
                if (((RakenneOsa) rakenneosa).getTutkinnonOsaViite().equals(viite)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.tutkinnon_muodostuminen;
    }

    @Target({TYPE, ANNOTATION_TYPE})
    @Retention(RUNTIME)
    @Constraint(validatedBy = {ValidRakenneModuuliValidator.class})
    @Documented
    public @interface ValidRakenneModuuli {
        String message() default "{org.hibernate.validator.referenceguide.chapter06.classlevel." +
                "ValidRakenneModuuliValidator.message}";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }

    public static class ValidRakenneModuuliValidator implements ConstraintValidator<ValidRakenneModuuli, RakenneModuuli> {

        public ValidRakenneModuuliValidator() {

        }

        @Override
        public void initialize(ValidRakenneModuuli validRakenneModuuliValidator) {

        }

        @Override
        public boolean isValid(RakenneModuuli moduuli, ConstraintValidatorContext constraintValidatorContext) {
            boolean hasTutkintonimikeAndOsaamisala = moduuli.getTutkintonimike() != null && moduuli.getOsaamisala() != null;
            boolean hasCorrectTutkintonimike = moduuli.getTutkintonimike() == null || moduuli.getRooli() == RakenneModuuliRooli.TUTKINTONIMIKE;
            boolean hasCorrectOsaamisala = moduuli.getOsaamisala() == null || moduuli.getRooli() == RakenneModuuliRooli.OSAAMISALA;
            return !hasTutkintonimikeAndOsaamisala && hasCorrectOsaamisala && hasCorrectTutkintonimike;

        }
    }
}
