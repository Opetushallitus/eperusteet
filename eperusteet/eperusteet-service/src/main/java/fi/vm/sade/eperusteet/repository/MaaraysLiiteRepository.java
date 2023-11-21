package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.maarays.MaaraysLiite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MaaraysLiiteRepository extends JpaRepository<MaaraysLiite, UUID> {
}
