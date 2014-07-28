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

import java.io.Serializable;
import java.util.List;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface JpaWithVersioningRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

	List<Revision> getRevisions(final ID id);
	T findRevision(final ID id, final Integer revisionId);

	public class DomainClassNotAuditedException extends BeanCreationException {

		public DomainClassNotAuditedException(Class<?> clazz) {
			super("Defined domain class '" + clazz.getSimpleName() + "' does not contain @audited-annotation");
		}
	}

}
