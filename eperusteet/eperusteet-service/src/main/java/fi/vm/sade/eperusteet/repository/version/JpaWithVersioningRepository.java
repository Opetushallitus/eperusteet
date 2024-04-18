package fi.vm.sade.eperusteet.repository.version;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface JpaWithVersioningRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

    List<Revision> getRevisions(final ID id);

    T findRevision(final ID id, final Integer revisionId);

    Revision getLatestRevisionId(final ID id);

    T getLatestNotNull(ID id);

    class DomainClassNotAuditedException extends BeanCreationException {

        public DomainClassNotAuditedException(Class<?> clazz) {
            super("Defined domain class '" + clazz.getSimpleName() + "' does not contain @audited-annotation");
        }
    }

    /**
     * Lukitsee entiteetin muokkausta varten. Lukitus vapautuu automaattisesti transaktion loppuessa.
     * <p>
     * Enversillä on ongelmia yhtäaikaisten transaktioiden kanssa, joten pessimistisen lukituksen käyttäminen on joissakin tapauksissa tarpeen.
     *
     * @param entity
     * @param refresh -- päivitetääkö entiteetti lukitsemisen yhteydesssä tietokannasta.
     * @return päivitetty, lukittu entiteetti
     */
    @Transactional(propagation = Propagation.MANDATORY)
    T lock(T entity, boolean refresh);

    /**
     * Lukitsee entiteetin muokkausta varten. Lukitus vapautuu automaattisesti transaktion loppuessa.
     * Sama kuin lock(entity, true).
     *
     * @see #lock(java.lang.Object, boolean)
     */
    @Transactional(propagation = Propagation.MANDATORY)
    T lock(T entity);

    /**
     * Asettaa revisiokohtaisen kommentin.
     *
     * Jos revisioon on jo asetettu kommentti, uusi kommentti korvaa aikaisemman. Kommentti on globaali koko revisiolle; jos samassa muutoksessa muokataan
     * useita entiteettejä, kommentti koskee niitä kaikkia.
     *
     * @param kommentti valinnainen kommentti
     */
    void setRevisioKommentti(String kommentti);

    /**
     * Palauttaa viimeisimmän tietokannan versionumeron
     */
    int getLatestRevisionId();

    default T findOne(ID id) {
        return findById(id).orElse(null);
    }
    
}
