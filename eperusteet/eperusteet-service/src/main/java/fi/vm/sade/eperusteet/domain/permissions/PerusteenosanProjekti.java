package fi.vm.sade.eperusteet.domain.permissions;

import fi.vm.sade.eperusteet.domain.ProjektiTila;
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

/**
 * Perusteenosaan liittyvät projektit
 * Taulu esittää todellisuudessa näkymää
 */
@Entity
@Table(name = "perusteenosa_projekti")
@Immutable
@Getter
@IdClass(PerusteenosanProjekti.Key.class)
public class PerusteenosanProjekti {

    @Id
    private Long id;

    @Id
    @Column(name = "perusteprojekti_id")
    private Long perusteProjektiId;

    private boolean esikatseltavissa = false;

    @Column(name = "ryhmaoid")
    private String ryhmaOid;

    @Enumerated(EnumType.STRING)
    private ProjektiTila tila;

    @EqualsAndHashCode
    public static class Key implements Serializable {

        public Key(Long id, Long perusteProjektiId) {
            this.id = id;
            this.perusteProjektiId = perusteProjektiId;
        }

        public Key() {
        }

        private Long id;
        private Long perusteProjektiId;
    }

}
