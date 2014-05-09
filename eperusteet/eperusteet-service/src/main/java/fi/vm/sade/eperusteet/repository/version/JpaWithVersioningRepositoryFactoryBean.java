package fi.vm.sade.eperusteet.repository.version;

import java.io.Serializable;

import javax.persistence.EntityManager;

import org.hibernate.envers.Audited;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository.DomainClassNotAuditedException;

public class JpaWithVersioningRepositoryFactoryBean<R extends JpaRepository<T, ID>, T, ID extends Serializable> extends JpaRepositoryFactoryBean<R, T, ID> {

    @Override
    protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {

        return new JpaWithVersioningRepositoryFactory<>(entityManager);
    }

    private static class JpaWithVersioningRepositoryFactory<T, ID extends Serializable> extends JpaRepositoryFactory {

        private final EntityManager entityManager;

        public JpaWithVersioningRepositoryFactory(EntityManager entityManager) {
            super(entityManager);

            this.entityManager = entityManager;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Object getTargetRepository(RepositoryMetadata metadata) {

            if (JpaWithVersioningRepository.class.isAssignableFrom(metadata.getRepositoryInterface())) {
                if (metadata.getDomainType().getAnnotation(Audited.class) == null) {
                    throw new DomainClassNotAuditedException(metadata.getDomainType());
                }
                return new JpaWithVersioningRepositoryImpl<>((JpaEntityInformation<T, ID>) getEntityInformation((Class<T>) metadata.getDomainType()), entityManager);
            } else {
                return super.getTargetRepository(metadata);
            }
        }

        @Override
        protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
            return JpaWithVersioningRepository.class.isAssignableFrom(metadata.getRepositoryInterface())
                ? JpaWithVersioningRepository.class : super.getRepositoryBaseClass(metadata);
        }
    }
}
