package fi.vm.sade.eperusteet.hibernate;

import fi.ratamaa.dtoconverter.reflection.Property;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetadataIntegrator implements Integrator {
    private static Map<Class<?>, List<Property>> anywhareReferencedFromProperties = new HashMap<>();

    @Override
    public void integrate(Configuration configuration, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
        configuration.getClassMappings().forEachRemaining(
                mapping -> Property.mapForClass(mapping.getMappedClass())
                        .values().stream().filter(p -> p.getContainedTypeOrType()
                                .isAnnotationPresent(RelatesToPeruste.FromAnywhereReferenced.class)
                            && !p.isAnnotationPresent(Transient.class))
                        .forEach(p -> classPropertyList(p.getContainedTypeOrType()).add(p)));
    }

    private List<Property> classPropertyList(Class<?> clz) {
        List<Property> list = anywhareReferencedFromProperties.get(clz);
        if (list == null) {
            list = new ArrayList<>();
            anywhareReferencedFromProperties.put(clz, list);
        }
        return list;
    }

    public static List<Property> findPropertiesReferencingTo(Class<?> clz) {
        List<Property> list = anywhareReferencedFromProperties.get(clz);
        return list == null ? new ArrayList<>() : list;
    }

    @Override
    public void integrate(MetadataImplementor metadata, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
        anywhareReferencedFromProperties = new HashMap<>();
    }
}
