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
package fi.vm.sade.eperusteet.resource;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author jhyoty
 */
@Controller
public class VirheController {

    @RequestMapping(value = "/WEB-INF/virhe")
    @ResponseBody
    public Map<String, Object> handle(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();                        
        map.put("koodi", request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE));
        Object e = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        if ( e instanceof Throwable ) {
            map.put("syy", ((Throwable)e).getLocalizedMessage());
        } else {
            map.put("syy", request.getAttribute(RequestDispatcher.ERROR_MESSAGE));
        }
        return map;
    }

}
