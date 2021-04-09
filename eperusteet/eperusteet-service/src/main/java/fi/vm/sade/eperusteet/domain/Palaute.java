package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.domain.permissions.PerusteenosanProjekti;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Audited
@Table(name = "palaute")
@IdClass(Palaute.Key.class)
public class Palaute extends AbstractAuditedEntity {

    @Id
    private Date createdAt;

    @Id
    private Integer stars;

    @Id
    private String key;

    @Enumerated(EnumType.STRING)
    private PalauteStatus status;

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Key implements Serializable {

        @Id
        private Date createdAt;

        @Id
        private Integer stars;

        @Id
        private String key;
    }
}
