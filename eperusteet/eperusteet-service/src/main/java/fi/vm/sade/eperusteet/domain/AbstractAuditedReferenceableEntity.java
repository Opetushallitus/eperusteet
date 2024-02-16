package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.dto.Reference;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractAuditedReferenceableEntity extends AbstractAuditedEntity implements ReferenceableEntity {

    @Id
    @Getter
    @Setter
    @Audited
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Override
    public Reference getReference() {
        return new Reference(id);
    }

}
