package fi.vm.sade.eperusteet.resource.util;

import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class CacheHeaderInterceptor implements HandlerInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(CacheHeaderInterceptor.class);

    public CacheHeaderInterceptor() {
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            final HandlerMethod handlerMethod = (HandlerMethod) handler;
            if (!returnsResponseEntity(handlerMethod)) {
                //jos palautetaan ResponsEntity, annetaan mahdollisuus muokata vastausta sitä kautta
                CacheControl cc = handlerMethod.getMethodAnnotation(CacheControl.class);
                long date = 0;
                String cacheControl = CacheControls.PRIVATE_NOT_CACHEABLE;
                if (cc != null) {
                    date = (new Date().getTime()) + cc.age() * 1000;
                    cacheControl = CacheControls.buildCacheControl(cc);
                }
                response.setDateHeader(com.google.common.net.HttpHeaders.EXPIRES, date);
                response.setHeader(com.google.common.net.HttpHeaders.CACHE_CONTROL, cacheControl);
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws
        Exception {
        //Posthandlessa ei voi enää muokata vastausta
        //@see CacheHeadersAspect
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws
        Exception {
    }

    private static boolean returnsResponseEntity(HandlerMethod handlerMethod) {
        return !handlerMethod.isVoid()
            && ResponseEntity.class.isAssignableFrom(handlerMethod.getReturnType().getParameterType());
    }

}
