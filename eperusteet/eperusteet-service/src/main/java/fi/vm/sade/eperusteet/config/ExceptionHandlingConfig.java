package fi.vm.sade.eperusteet.config;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.google.common.base.Throwables;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.service.exception.LockingException;
import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import fi.vm.sade.eperusteet.service.exception.ServiceException;
import fi.vm.sade.eperusteet.service.internal.LockManager;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MappingException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NestedCheckedException;
import org.springframework.core.NestedRuntimeException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.persistence.PersistenceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class ExceptionHandlingConfig extends ResponseEntityExceptionHandler {

    @Autowired
    private LockManager lukkomanageri;

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<Object> handleTransactionExceptions(TransactionSystemException e, WebRequest request) {
        if (e.getRootCause() != null && e.getRootCause() instanceof ConstraintViolationException) {
            return handleExceptionInternal((ConstraintViolationException) e.getRootCause(), null, new HttpHeaders(),
                                           HttpStatus.BAD_REQUEST, request);
        } else {
            return handleExceptionInternal(e, null, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
        }
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        if (ex.getRootCause() != null && ex.getRootCause() instanceof UnrecognizedPropertyException) {
            return handleExceptionInternal((UnrecognizedPropertyException) ex.getRootCause(), null, headers, status, request);
        } else {
            return handleExceptionInternal(ex, null, headers, status, request);
        }
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Object> clientAbortExceptionHandler(HttpServletRequest request, WebRequest webRequest, IOException ex) throws Exception {
        String exceptionSimpleName = ex.getCause().getClass().getSimpleName();
        if ("ClientAbortException".equals(exceptionSimpleName)) {
            Principal principal = request.getUserPrincipal();
            String username = principal != null ? principal.getName() : "<NONE>";
            log.warn("ClientAbortException: message={} username={}, remoteAddr={}, userAgent={}, requestedURL={}",
                    ex.getLocalizedMessage(), username, request.getRemoteAddr(), request.getHeader("User-Agent"),
                    request.getRequestURL());
            return null;
        } else {
            return handleAllExceptions(ex, webRequest);
        }
    }

    @ExceptionHandler(value = {
            Exception.class
    })
    public ResponseEntity<Object> handleAllExceptions(Exception e, WebRequest request) throws Exception {
        if (e instanceof AuthenticationException || e instanceof AccessDeniedException) {
            throw e;
        }

        HttpStatus status = HttpStatus.BAD_REQUEST;
        ResponseStatus rs = e.getClass().getAnnotation(ResponseStatus.class);
        if (rs != null) {
            status = rs.value();
        }
        return handleExceptionInternal(e, null, new HttpHeaders(), status, request);
    }

    private void describe(Map<String, Object> map, String koodi) {
        describe(map, koodi, "");
    }

    private void describe(Map<String, Object> map, String koodi, String selkokielinen) {
        map.put("syy", selkokielinen);
        map.put("avain", koodi);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        final Map<String, Object> map = new HashMap<>();
        boolean suppresstrace = false;

        if (ex instanceof BindException) {
            describe(map, "server-virhe-datan-kytkemisessä", "Virhe datan kytkemisessä.");
        } else if (ex instanceof ConversionNotSupportedException) {
            describe(map, "datamuunnos-ei-ole-tuettu", "Datamuunnos ei ole tuettu.");
        } else if (ex instanceof HttpMediaTypeNotAcceptableException) {
            describe(map, "mediatyyppi-ei-ole-hyväksytty", "Mediatyyppi ei ole hyväksytty.");
        } else if (ex instanceof HttpMediaTypeNotSupportedException) {
            describe(map, "mediatyyppi-ei-ole-tuettu", "Mediatyyppi ei ole tuettu.");
        } else if (ex instanceof HttpMessageNotReadableException) {
            status = HttpStatus.BAD_REQUEST;
            describe(map, "http-viestiä-ei-pystytty-lukemaan", "virheellinen pyyntö");
        } else if (ex instanceof HttpMessageNotWritableException) {
            describe(map, "http-viestiä-ei-pystytty-kirjoittamaan", "Http-viestiä ei pystytty kirjoittamaan.");
        } else if (ex instanceof HttpRequestMethodNotSupportedException) {
            describe(map, "palvelin-ei-pystynyt-käsittelemään-pyyntöä", "Palvelin ei pystynyt käsittelemään http-pyyntöä.");
        } else if (ex instanceof MethodArgumentNotValidException) {
            describe(map, "palvelin-ei-pystynyt-käsittelemään-pyyntöä", "Palvelin ei pystynyt käsittelemään http-pyyntöä.");
        } else if (ex instanceof MissingServletRequestParameterException) {
            describe(map, "pyynnöstä-puuttui-parametri", "Pyynnöstä puuttui parametri, eikä sitä voitu tästä syystä käsitellä.");
        } else if (ex instanceof MissingServletRequestPartException) {
            describe(map, "pyynnöstä-puuttui-osa", "Pyynnöstä puuttui osa, eikä sitä voitu tästä syystä käsitellä.");
        } else if (ex instanceof TypeMismatchException) {
            suppresstrace = true;
            describe(map, "tyypin-yhteensopivuusongelma", "Tyypin yhteensopivuusongelma.");
        } else if (ex instanceof TransactionSystemException) {
            describe(map, "datan-käsittelyssä-odottamaton-virhe", "Datan käsittelyssä tapahtui odottamaton virhe.");
        } else if (ex instanceof UnrecognizedPropertyException) {
            describe(map, "datassa-tuntematon-kenttä", "Dataa ei pystytty käsittelemään. Lähetetyssä datassa esiintyi tuntematon kenttä \"" +
                     ((UnrecognizedPropertyException) ex).getPropertyName() + "\"");
        } else if (ex instanceof ConstraintViolationException) {
            suppresstrace = true;
            List<String> reasons = new ArrayList<>();
            for (ConstraintViolation<?> constraintViolation : ((ConstraintViolationException) ex).getConstraintViolations()) {
                reasons.add(constraintViolation.getPropertyPath().toString() + ": " + constraintViolation.getMessage());
            }
            map.put("syy", reasons);
        } else if (ex instanceof UnsatisfiedServletRequestParameterException) {
            StringBuilder builder = new StringBuilder().append("Pyynnöstä puuttui parametrit \"");
            for (String violation : ((UnsatisfiedServletRequestParameterException) ex).getParamConditions()) {
                builder.append(violation).append(' ');
            }
            builder.append("\"");
            map.put("syy", builder.toString());
        } else if (ex instanceof LockingException) {
            suppresstrace = true;
            LockingException le = (LockingException) ex;
            map.put("syy", ex.getLocalizedMessage());
            map.put("avain", "server-lukitus");
            LukkoDto lukko = le.getLukko();
            if (lukko != null) {
                lukkomanageri.lisaaNimiLukkoon(lukko);
                map.put("lukko", lukko);
            }
        } else if (ex instanceof NotExistsException) {
            suppresstrace = true;
            map.put("syy", ex.getLocalizedMessage());
        } else if (ex instanceof ServiceException) {
            map.put("syy", ex.getLocalizedMessage());
        } else if (ex instanceof IllegalArgumentException) {
            suppresstrace = true;
            map.put("syy", ex.getLocalizedMessage());
        } else if (ex instanceof IOException) {
            if (StringUtils.containsIgnoreCase(ExceptionUtils.getRootCauseMessage(ex), "Broken pipe")) {
                suppresstrace = true;
                map.put("syy", ex.getLocalizedMessage());
                map.put("avain", "client-abort-virhe");
            }
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            map.put("syy", "Sovelluspalvelimessa tapahtui odottamaton virhe");
            map.put("avain", "server-odottamaton-virhe");
            map.put("koodi", status);
        }
        map.put("koodi", status);

        if (suppresstrace) {
            log.warn("Virhetilanne: {}", ex.getLocalizedMessage());
        } else {
            log.error("Virhetilanne: ", ex);
        }

        logRequest(request);
        return super.handleExceptionInternal(ex, map, headers, status, request);
    }

    public void logRequest(WebRequest webRequest) {
        if (webRequest instanceof ServletWebRequest) {
            ServletWebRequest servletWebRequest = (ServletWebRequest) webRequest;

            String method = servletWebRequest.getRequest().getMethod();
            String path = servletWebRequest.getRequest().getRequestURI();
            String queryString = servletWebRequest.getRequest().getQueryString();

            log.error("Request: {} {}?{}", method, path, (queryString != null ? queryString : ""));
        }
    }
}
