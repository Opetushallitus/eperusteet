package fi.vm.sade.eperusteet.config;

import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

@ControllerAdvice
public class InitBinderControllerAdvice {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Suoritustapakoodi.class, new EnumToUpperCaseEditor<>(Suoritustapakoodi.class));
    }

}
