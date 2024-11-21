package fi.vm.sade.eperusteet.service.mapping;

import fi.vm.sade.eperusteet.domain.ReferenceableEntity;
import fi.vm.sade.eperusteet.dto.Reference;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Inheritance;
import jakarta.persistence.PersistenceContext;

@Component
@Dto
public class ReferenceableEntityConverter extends BidirectionalConverter<ReferenceableEntity, Reference> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Reference convertTo(ReferenceableEntity source, Type<Reference> destinationType, MappingContext mappingContext) {
        return source.getReference();
    }

    @Override
    public ReferenceableEntity convertFrom(Reference source, Type<ReferenceableEntity> destinationType, MappingContext mappingContext) {
        if (destinationType.getRawType().isAnnotationPresent(Inheritance.class)) {
            // Perintähierarkioiden tapauksessa getReference() aiheuttaa ongelmia mappauksen kanssa
            // (viitteen luokka on perintähierarkian isäluokka eikä "oikea" luokka)
            ReferenceableEntity e = em.find(destinationType.getRawType(), Long.valueOf(source.getId()));
            if (e == null) {
                throw new IllegalArgumentException("Virheellinen viite " + source);
            }
        }
        return em.getReference(destinationType.getRawType(), Long.valueOf(source.getId()));
    }

    @Override
    public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
        return (this.sourceType.isAssignableFrom(sourceType) && this.destinationType.isAssignableFrom(destinationType))
            || (this.sourceType.isAssignableFrom(destinationType) && this.destinationType.isAssignableFrom(sourceType));
    }

}
