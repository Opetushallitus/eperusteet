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
package fi.vm.sade.eperusteet.service.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ylimmän tason virhekäsittelijä
 *
 * @author jhyoty
 */
public class VirheServlet extends HttpServlet {

    private final JsonFactory jsonFactory = new JsonFactory();
    private static final Logger LOG = LoggerFactory.getLogger(VirheServlet.class);

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (response.isCommitted()) {
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
        } catch (IllegalStateException e) {
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
                LOG.error("Käsittelemätön poikkeus: {}", t.getLocalizedMessage());
            }
            return t.getLocalizedMessage();
        }
        return request.getAttribute(RequestDispatcher.ERROR_MESSAGE).toString();
    }
}
