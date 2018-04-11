/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.domain.tutkinnonrakenne;

import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.Mergeable;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.persistence.*;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@Entity
@DiscriminatorValue("RM")
@Audited
@RakenneModuuli.ValidRakenneModuuli
public class RakenneModuuli extends AbstractRakenneOsa implements Mergeable<RakenneModuuli> {

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
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

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Koodi osaamisala;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
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
            this.nimi = moduuli.getNimi();
            this.setKuvaus(moduuli.getKuvaus());
            this.
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
    public boolean isSame(AbstractRakenneOsa moduuli, boolean excludeText) {
        return (moduuli instanceof RakenneModuuli) && isSame((RakenneModuuli) moduuli, excludeText);
    }

    public boolean isSame(RakenneModuuli moduuli, boolean excludeText) {
        if (!super.isSame(moduuli, excludeText)) {
            return false;
        }

        if (!excludeText && !Objects.equals(this.nimi, moduuli.getNimi())) {
            return false;
        }

        if ((this.osat == null && moduuli.getOsat() != null) || (this.osat != null && moduuli.getOsat() == null)) {
            return false;
        }

        if ((this.getPakollinen() == null && moduuli.getPakollinen() != null)
                || (this.getPakollinen() != null && moduuli.getPakollinen() == null)
                || this.getPakollinen() != moduuli.getPakollinen()) {
            return false;
        }

        if (this.getRooli() != moduuli.getRooli()) {
            return false;
        }

        if (!Objects.equals(this.getTutkintonimike(), moduuli.getTutkintonimike())) {
            return false;
        }

        if (!Objects.equals(this.getOsaamisala(), moduuli.getOsaamisala())) {
            return false;
        }

        if (!Objects.equals(this.muodostumisSaanto, moduuli.getMuodostumisSaanto())) {
            return false;
        }

        if (this.osat != null && moduuli.getOsat() != null) {
            if (this.osat.size() != moduuli.getOsat().size()) {
                return false;
            }

            Iterator<AbstractRakenneOsa> l = this.getOsat().iterator();
            Iterator<AbstractRakenneOsa> r = moduuli.getOsat().iterator();
            while (l.hasNext() && r.hasNext()) {
                if (!l.next().isSame(r.next(), excludeText)) {
                    return false;
                }
            }
        }

        return true;
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

    @Target({ TYPE, ANNOTATION_TYPE })
    @Retention(RUNTIME)
    @Constraint(validatedBy = { ValidRakenneModuuliValidator.class })
    @Documented
    public @interface ValidRakenneModuuli {
        String message() default "{org.hibernate.validator.referenceguide.chapter06.classlevel." +
                "ValidRakenneModuuliValidator.message}";
        Class<?>[] groups() default { };
        Class<? extends Payload>[] payload() default { };
    }

    private static class ValidRakenneModuuliValidator implements ConstraintValidator<ValidRakenneModuuli, RakenneModuuli> {
        @Override
        public void initialize(ValidRakenneModuuli validRakenneModuuliValidator) {

        }

        @Override
        public boolean isValid(RakenneModuuli moduuli, ConstraintValidatorContext constraintValidatorContext) {
            boolean hasTutkintonimikeAndOsaamisala = moduuli.getTutkintonimike() != null && moduuli.getOsaamisala() != null;
            boolean hasCorrectTutkintonimike = moduuli.getTutkintonimike() == null || moduuli.getRooli() == RakenneModuuliRooli.TUTKINTONIMIKE;
            boolean hasCorrectOsaamisala = moduuli.getOsaamisala() == null || moduuli.getRooli() == RakenneModuuliRooli.OSAAMISALA;
            return !hasTutkintonimikeAndOsaamisala && hasCorrectOsaamisala && hasCorrectOsaamisala;
        }
    }
}
