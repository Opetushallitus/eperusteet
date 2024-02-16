package fi.vm.sade.eperusteet.dto.util;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collection;
import java.util.stream.Stream;

public interface Lokalisoitava {
    @JsonIgnore
    Stream<LokalisoituTekstiDto> lokalisoitavatTekstit();

    static Lokalisoitava of(Lokalisoitava lokalisoitava) {
        return () -> lokalisoitava == null ? Stream.of() : lokalisoitava.lokalisoitavatTekstit();
    }
    static Lokalisoitava of(LokalisoituTekstiDto... tekstit) {
        return () -> notNull(Stream.of(tekstit));
    }
    static Lokalisoitava of(Collection<? extends Lokalisoitava> of) {
        return () -> notNull(of.stream()).flatMap(Lokalisoitava::lokalisoitavatTekstit);
    }
    default Lokalisoitava and(LokalisoituTekstiDto... teksti) {
        return and(notNull(Stream.of(teksti)));
    }
    default Lokalisoitava and(Lokalisoitava... and) {
        return and(notNull(Stream.of(and)).flatMap(Lokalisoitava::lokalisoitavatTekstit));
    }
    default Lokalisoitava and(Collection<? extends Lokalisoitava> and) {
        return and(notNull(and.stream()).flatMap(Lokalisoitava::lokalisoitavatTekstit));
    }
    static<T> Stream<T> notNull(Stream<T> stream) {
        return stream.filter(v -> v != null);
    }
    default Lokalisoitava and(Stream<LokalisoituTekstiDto> stream) {
        return () -> Stream.concat(lokalisoitavatTekstit(), stream);
    }
}
