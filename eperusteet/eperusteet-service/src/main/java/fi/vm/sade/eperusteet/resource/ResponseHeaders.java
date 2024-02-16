package fi.vm.sade.eperusteet.resource;

import java.util.concurrent.TimeUnit;
import org.joda.time.DateTime;
import org.springframework.http.HttpHeaders;

public final class ResponseHeaders {

    private ResponseHeaders() {
        //apuluokka
    }

    public static HttpHeaders cacheHeaders(int aika, TimeUnit unit) {
        return cacheHeaders(aika, unit, new HttpHeaders());
    }

    public static HttpHeaders cacheHeaders(int aika, TimeUnit unit, HttpHeaders headers) {
        final int ms = (int)Math.min(TimeUnit.MILLISECONDS.convert(aika, unit), Integer.MAX_VALUE);
        headers.setExpires(DateTime.now().plusMillis(ms).getMillis());
        headers.setCacheControl(String.format("max-age=%d", ms / 1000));
        return headers;
    }

}
