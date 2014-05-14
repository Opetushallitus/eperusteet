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
