/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.repository.version;

import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository.DomainClassNotAuditedException;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import jakarta.persistence.EntityManager;
import java.io.Serializable;

public class JpaWithVersioningRepositoryFactoryBean<R extends JpaRepository<T, ID>, T, ID extends Serializable> extends JpaRepositoryFactoryBean<R, T, ID> {

    /**
     * Creates a new {@link JpaRepositoryFactoryBean} for the given repository interface.
     *
     * @param repositoryInterface must not be {@literal null}.
     */
    public JpaWithVersioningRepositoryFactoryBean(Class<? extends R> repositoryInterface) {
        super(repositoryInterface);
    }

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
        protected JpaRepositoryImplementation<?, ?> getTargetRepository(RepositoryInformation metadata, EntityManager entityManager) {

            if (JpaWithVersioningRepository.class.isAssignableFrom(metadata.getRepositoryInterface())) {
                if (metadata.getDomainType().getAnnotation(Audited.class) == null) {
                    throw new DomainClassNotAuditedException(metadata.getDomainType());
                }
                return new JpaWithVersioningRepositoryImpl<>(getEntityInformation((Class<T>) metadata.getDomainType()), entityManager);
            } else {
                return super.getTargetRepository(metadata, entityManager);
            }
        }

        @Override
        protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
            return JpaWithVersioningRepository.class.isAssignableFrom(metadata.getRepositoryInterface())
                ? JpaWithVersioningRepository.class : super.getRepositoryBaseClass(metadata);
        }
    }
}
