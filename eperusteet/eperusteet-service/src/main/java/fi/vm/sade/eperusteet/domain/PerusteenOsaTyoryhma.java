package fi.vm.sade.eperusteet.domain;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
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
    @ManyToOne(fetch = FetchType.LAZY)
    private Perusteprojekti perusteprojekti;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    private PerusteenOsa perusteenosa;

    @Getter
    @Setter
    @NotNull(message = "Työryhmän nimi ei voi olla tyhjä")
    @Column(name = "nimi")
    private String nimi;
}
