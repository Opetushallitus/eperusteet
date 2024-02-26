package fi.vm.sade.eperusteet.repository.custom;

import java.time.Duration;
import java.util.*;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import fi.vm.sade.eperusteet.repository.OphSessionMappingStorage;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;

public class OphSessionMappingStorageImpl implements OphSessionMappingStorage {

  private final JdbcTemplate jdbcTemplate;
  private final SessionRepository<? extends Session> sessionRepository;

  public OphSessionMappingStorageImpl(
      JdbcTemplate jdbcTemplate, SessionRepository<? extends Session> sessionRepository) {
    this.jdbcTemplate = jdbcTemplate;
    this.sessionRepository = sessionRepository;
  }

  private <T> Optional<T> queryForOptional(String sql, Class<T> requiredType, Object... args) {
    List<T> results = jdbcTemplate.query(sql, new SingleColumnRowMapper<>(requiredType), args);
    int size = results.size();
    if (size == 0) {
      return Optional.empty();
    }
    if (size > 1) {
      throw new IncorrectResultSizeDataAccessException(1, size);
    }
    return Optional.of(results.get(0));
  }

  @Override
  public HttpSession removeSessionByMappingId(String mappingId) {
    String sql = "SELECT session_id FROM cas_client_session WHERE mapping_id = ?";
    return queryForOptional(sql, String.class, mappingId)
        .map(sessionRepository::findById)
        .filter(Objects::nonNull)
        .map(session -> new HttpSessionAdapter(sessionRepository, session))
        .orElse(null);
  }

  @Override
  public void removeBySessionById(String sessionId) {
    String sql = "DELETE FROM cas_client_session WHERE session_id = ?";
    jdbcTemplate.update(sql, sessionId);
  }

  @Override
  public void addSessionById(String mappingId, HttpSession session) {
    String sql =
        "INSERT INTO cas_client_session (mapping_id, session_id) VALUES (?, ?) ON CONFLICT (mapping_id) DO NOTHING";
    jdbcTemplate.update(sql, mappingId, session.getId());
  }

  @Override
  public void clean() {
    String sql =
        "DELETE FROM cas_client_session WHERE session_id NOT IN (SELECT session_id FROM spring_session)";
    jdbcTemplate.update(sql);
  }

  @SuppressWarnings("deprecation")
  private static class HttpSessionAdapter implements HttpSession {

    private final SessionRepository<? extends Session> sessionRepository;
    private final Session session;

    public HttpSessionAdapter(
        SessionRepository<? extends Session> sessionRepository, Session session) {
      this.sessionRepository = sessionRepository;
      this.session = session;
    }

    @Override
    public long getCreationTime() {
      return session.getCreationTime().toEpochMilli();
    }

    @Override
    public String getId() {
      return session.getId();
    }

    @Override
    public long getLastAccessedTime() {
      return session.getLastAccessedTime().toEpochMilli();
    }

    @Override
    public ServletContext getServletContext() {
      throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
      session.setMaxInactiveInterval(Duration.ofSeconds(interval));
    }

    @Override
    public int getMaxInactiveInterval() {
      return (int) session.getMaxInactiveInterval().getSeconds();
    }

    @Override
    public javax.servlet.http.HttpSessionContext getSessionContext() {
      return new EmptyHttpSessionContext();
    }

    @Override
    public Object getAttribute(String name) {
      return session.getAttribute(name);
    }

    @Override
    public Object getValue(String name) {
      return getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
      return Collections.enumeration(session.getAttributeNames());
    }

    @Override
    public String[] getValueNames() {
      Set<String> attributeNames = session.getAttributeNames();
      return attributeNames.toArray(new String[attributeNames.size()]);
    }

    @Override
    public void setAttribute(String name, Object value) {
      session.setAttribute(name, value);
    }

    @Override
    public void putValue(String name, Object value) {
      setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
      session.removeAttribute(name);
    }

    @Override
    public void removeValue(String name) {
      removeAttribute(name);
    }

    @Override
    public void invalidate() {
      sessionRepository.deleteById(session.getId());
    }

    @Override
    public boolean isNew() {
      return false;
    }
  }

  @SuppressWarnings("deprecation")
  private static class EmptyHttpSessionContext implements javax.servlet.http.HttpSessionContext {

    @Override
    public HttpSession getSession(String sessionId) {
      return null;
    }

    @Override
    public Enumeration<String> getIds() {
      return Collections.emptyEnumeration();
    }
  }
}