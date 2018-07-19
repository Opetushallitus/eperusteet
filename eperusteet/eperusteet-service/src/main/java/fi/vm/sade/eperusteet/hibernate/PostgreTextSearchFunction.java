package fi.vm.sade.eperusteet.hibernate;

import fi.vm.sade.eperusteet.domain.Kieli;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.QueryException;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.BooleanType;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

import java.util.List;

@Slf4j
public class PostgreTextSearchFunction implements SQLFunction {
    @Override
    public boolean hasArguments() {
        return true;
    }

    @Override
    public boolean hasParenthesesIfNoArguments() {
        return false;
    }

    @Override
    public Type getReturnType(Type type, Mapping mapping) throws QueryException {
        return StandardBasicTypes.TRUE_FALSE;
    }

    private String kieliToPostgresFTSLang(Kieli kieli) {
        switch (kieli) {
            case FI: return "finnish";
            case SV: return "swedish";
            case SE: return "finnish";
            case RU: return "russian";
            case EN: return "english";
        }
        return null;
    }

    @Override
    public String render(Type type, List list, SessionFactoryImplementor sessionFactoryImplementor) throws QueryException {
        if (list.size() != 2) {
            throw new IllegalArgumentException("Text search needs language, target and query parameter");
        }

//        String kieli = kieliToPostgresFTSLang((Kieli) list.get(0));
        String target = (String) list.get(0);
        String parameter = (String) list.get(1);

        StringBuilder query = new StringBuilder();

        query.append("to_tsvector(")
                .append("'simple'")
                .append(", ").append(target).append(")")
                .append(" @@ ")
                .append(" plainto_tsquery(")
                .append("'simple'")
                .append(", ").append(parameter).append(")");

        return query.toString();
    }
}
