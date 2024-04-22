package fi.vm.sade.eperusteet.resource.util;

import org.springframework.http.HttpHeaders;

public final class Etags {

    private static final String WEAK_ETAG_PREFIX = "W/\"";

    private Etags() {
        //apuluokka
    }

    public static Integer revisionOf(String eTag) {
        if (eTag == null) {
            return null;
        }
        if (eTag.length() > 4 && eTag.startsWith(WEAK_ETAG_PREFIX)) {
            try {
                return Integer.parseInt(eTag.substring(3, eTag.length() - 1));
            } catch (NumberFormatException nfe) {
                //Ignore
            }
        }
        return null;
    }

    public static String eTagOf(Number rev) {
        return wrap(String.valueOf(rev));
    }

    public static HttpHeaders eTagHeader(Number revision) {
        return addETag(new HttpHeaders(), revision);
    }

    public static HttpHeaders addETag(HttpHeaders headers, Number revision) {
        if (revision != null) {
            headers.set("ETag", wrap(String.valueOf(revision)));
        }
        return headers;
    }

    private static String wrap(String value) {
        return WEAK_ETAG_PREFIX + value + "\"";
    }

}
