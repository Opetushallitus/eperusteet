package fi.vm.sade.eperusteet.service.mapping;

import fi.vm.sade.eperusteet.domain.ReferenceableEntity;
import fi.vm.sade.eperusteet.dto.Reference;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.Inheritance;
import javax.persistence.PersistenceContext;

@Component
@UncachedDto
public class ReferencableUncachedEntityConverter extends BidirectionalConverter<ReferenceableEntity, Reference> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Reference convertTo(ReferenceableEntity source, Type<Reference> destinationType, MappingContext mappingContext) {
        return source.getReference();
    }

    @Override
    public ReferenceableEntity convertFrom(Reference source, Type<ReferenceableEntity> destinationType, MappingContext mappingContext) {
        return null;
    }

    @Override
    public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
        return (this.sourceType.isAssignableFrom(sourceType) && this.destinationType.isAssignableFrom(destinationType))
                || (this.sourceType.isAssignableFrom(destinationType) && this.destinationType.isAssignableFrom(sourceType));
    }
}