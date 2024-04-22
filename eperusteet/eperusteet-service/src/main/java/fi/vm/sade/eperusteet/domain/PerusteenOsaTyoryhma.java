package fi.vm.sade.eperusteet.domain;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "perusteenosa_tyoryhma")
public class PerusteenOsaTyoryhma implements Serializable {
    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    private Perusteprojekti perusteprojekti;

    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    private PerusteenOsa perusteenosa;

    @Getter
    @Setter
    @NotNull(message = "Työryhmän nimi ei voi olla tyhjä")
    @Column(name = "nimi")
    private String nimi;
}
