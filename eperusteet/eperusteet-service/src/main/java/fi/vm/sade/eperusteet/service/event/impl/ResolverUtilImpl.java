package fi.vm.sade.eperusteet.service.event.impl;

import fi.ratamaa.dtoconverter.reflection.Property;
import fi.vm.sade.eperusteet.hibernate.HibernateInterceptor;
import fi.vm.sade.eperusteet.hibernate.MetadataIntegrator;
import fi.vm.sade.eperusteet.service.event.ResolvableReferenced;
import fi.vm.sade.eperusteet.service.event.ResolverUtil;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ResolverUtilImpl implements ResolverUtil {
    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional(readOnly = true)
    public Set<Long> findPerusteIdsByFirstResolvable(ResolvableReferenced resolvable) {
        Set<Long> ids = new HashSet<>();
        for (Property referencingProperty : MetadataIntegrator.findPropertiesReferencingTo(resolvable.getEntityClass())) {
            List owners = em.createQuery("select t from "
                    + referencingProperty.getDirectDeclaringClass().getSimpleName()
                    + " t where t." + referencingProperty.getName() + ".id = :id")
                .setParameter("id", resolvable.getId()).getResultList();
            for (Object entity : owners) {
                // most likely none
                HibernateInterceptor.findRelatedPeruste(entity, ids::add);
                if (!ids.isEmpty()) {
                    return ids;
                }
            }
        }
        return ids;
    }
}
