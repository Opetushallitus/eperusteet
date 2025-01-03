package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.dto.Reference;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

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
