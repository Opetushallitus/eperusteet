package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.dto.Reference;
import java.io.Serializable;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractReferenceableEntity implements ReferenceableEntity, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    protected Long id;

    @Override
    public Reference getReference() {
        return new Reference(id);
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        if (this.id == null) {
            this.id = id;
        }

    }

}
