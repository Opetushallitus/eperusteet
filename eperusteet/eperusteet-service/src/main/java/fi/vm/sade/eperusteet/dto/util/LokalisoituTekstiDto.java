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
package fi.vm.sade.eperusteet.dto.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import lombok.Getter;

/**
 *
 * @author jhyoty
 */
public class LokalisoituTekstiDto {
    private static final Map<Kieli,String> emptyMap = new EnumMap<>(Kieli.class);

    @Getter
    private final Long id;

    @Getter
    private UUID tunniste;

    @Getter
    private final Map<Kieli, String> tekstit;

    public LokalisoituTekstiDto(Long id, Map<Kieli, String> values) {
        this(id, null, values);
    }

    public LokalisoituTekstiDto(Long id, UUID tunniste, Map<Kieli, String> values) {
        this.id = id;
        this.tunniste = tunniste;
        this.tekstit = values == null ? null : new EnumMap<>(values);
    }

    @JsonCreator
    public LokalisoituTekstiDto(Map<String, String> values) {
        Long tmpId = null;
        EnumMap<Kieli, String> tmpValues = new EnumMap<>(Kieli.class);

        if (values != null) {
            for (Map.Entry<String, String> entry : values.entrySet()) {
                if ("_id".equals(entry.getKey())) {
                    tmpId = Long.valueOf(entry.getValue());
                }
                else if ("_tunniste".equals(entry.getKey())) {
                    this.tunniste = UUID.fromString(entry.getValue());
                }
                else {
                    Kieli k = Kieli.of(entry.getKey());
                    tmpValues.put(k, entry.getValue());
                }
            }
        }

        this.id = tmpId;
        this.tekstit = tmpValues;
    }

    @JsonValue
    public Map<String, String> asMap() {
        HashMap<String, String> map = new HashMap<>();
        if (id != null) {
            map.put("_id", id.toString());
        }
        if (tunniste != null) {
            map.put("_tunniste", tunniste.toString());
        }
        for (Map.Entry<Kieli, String> e : tekstit.entrySet()) {
            map.put(e.getKey().toString(), e.getValue());
        }
        return map;
    }

    public LokalisoituTekstiDto concat(Function<Kieli,String> str) {
        Map<Kieli,String> transformed = new HashMap<>();
        for (Map.Entry<Kieli,String> kv : tekstit.entrySet()) {
            transformed.put(kv.getKey(), kv.getValue() + str.apply(kv.getKey()));
        }
        return new LokalisoituTekstiDto(id, null, transformed);
    }

    public LokalisoituTekstiDto concat(String str) {
        return concat(anyKieli -> str);
    }

    public LokalisoituTekstiDto concat(LokalisoituTekstiDto dto) {
        return concat(dto::get);
    }

    @JsonIgnore
    public String get(Kieli kieli) {
        return tekstit.get(kieli);
    }

    public boolean containsKey(Kieli kieli) {
        return tekstit.containsKey(kieli);
    }

    public static LokalisoituTekstiDto localizeLaterById(Long id) {
        return id == null ? null : new LokalisoituTekstiDto(id, null, emptyMap);
    }

    @SuppressWarnings("DtoClassesNotContainEntities")
    public static LokalisoituTekstiDto localized(TekstiPalanen palanen) {
        return palanen == null ? null : new LokalisoituTekstiDto(palanen.getId(), palanen.getTunniste(), palanen.getTeksti());
    }

    public interface LocalizedFunction<F> extends Function<F,LokalisoituTekstiDto> {
        default LocalizedFunction<F> concat(String constant) {
            return from -> this.apply(from).concat(anyKieli -> constant);
        }
        default LocalizedFunction<F> concat(LokalisoituTekstiDto dto) {
            return from -> this.apply(from).concat(dto::get);
        }
    }

    @SuppressWarnings("DtoClassesNotContainEntities")
    public static<T> LocalizedFunction<T> localized(Function<T,TekstiPalanen> s) {
        return from -> localized(s.apply(from));
    }
}
