package fi.vm.sade.eperusteet.service.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.base.Throwables;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ylimmän tason virhekäsittelijä
 */
public class VirheServlet extends HttpServlet {

    private final JsonFactory jsonFactory = new JsonFactory();
    private static final Logger LOG = LoggerFactory.getLogger(VirheServlet.class);

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        if ( response.isCommitted() ) {
            return;
        }
        response.resetBuffer();
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            JsonGenerator json = jsonFactory.createGenerator(out);
            json.writeStartObject();
            json.writeStringField("koodi", request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE).toString());
            json.writeStringField("syy", getErrorMessage(request));
            json.writeEndObject();
            json.flush();
        } catch ( IllegalStateException e ) {
            //NOP
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //NOP
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //NOP
    }

    private static String getErrorMessage(HttpServletRequest request) {
        Object e = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        if (e instanceof Throwable) {
            final Throwable t = (Throwable) e;
            if (LOG.isDebugEnabled()) {
                LOG.error("Käsittelemätön poikkeus: ", t);
            } else {
                LOG.error("Käsittelemätön poikkeus: {}", Throwables.getStackTraceAsString(t));
            }
            return t.getLocalizedMessage();
        }
        return request.getAttribute(RequestDispatcher.ERROR_MESSAGE).toString();
    }
}
