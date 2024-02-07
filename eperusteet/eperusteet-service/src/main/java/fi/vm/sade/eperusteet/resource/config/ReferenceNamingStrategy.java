package fi.vm.sade.eperusteet.resource.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import fi.vm.sade.eperusteet.dto.Reference;

import java.lang.reflect.Type;
import java.util.Optional;

/**
 * JSON-kenttien nimeämisstrategia.
 * <p>
 * Nimeää EntitiReference -tyyppiä olevat kentät muotoon _kentännimi ja käyttää muissa tapauksissa oletusnimeämistä.
 */
public class ReferenceNamingStrategy extends PropertyNamingStrategy {

    @Override
    public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        return getName(config, method.getType(), defaultName);
    }

    @Override
    public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        return getName(config, method.getParameter(0).getType(), defaultName);
    }

    private String getName(MapperConfig<?> config, Type type, String defaultName) {
        final JavaType et = config.getTypeFactory().constructType(Reference.class);
        final JavaType ot = config.getTypeFactory().constructReferenceType(Optional.class, et);
        // Todo: Use only java.util.Optional
        final JavaType googleOt = config.getTypeFactory()
                .constructReferenceType(com.google.common.base.Optional.class, et);
        final JavaType t = config.getTypeFactory().constructType(type);

        if (et.equals(t) || ot.equals(t) || googleOt.equals(t)) {
            return "_" + defaultName;
        }

        return defaultName;
    }

}
