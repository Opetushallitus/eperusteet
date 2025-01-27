package fi.vm.sade.eperusteet.repository;


import org.apereo.cas.client.session.SessionMappingStorage;

public interface OphSessionMappingStorage extends SessionMappingStorage {

  void clean();
}