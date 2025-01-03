package fi.vm.sade.eperusteet.resource.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class ProfilingInterceptor implements HandlerInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger("fi.vm.sade.eperusteet.PROFILING");

    public ProfilingInterceptor() {
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (LOG.isTraceEnabled()) {
            request.setAttribute(this.getClass().getCanonicalName(), System.currentTimeMillis());
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws
        Exception {
        //NOP
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws
        Exception {
        if (LOG.isTraceEnabled()) {
            long t0 = (Long) request.getAttribute(this.getClass().getCanonicalName());
            LOG.trace(String.format("Request %s %s took %d ms (handler: %s)",request.getMethod(), request.getRequestURI(), (System.currentTimeMillis() - t0), handler.toString()));
        }
    }

}
