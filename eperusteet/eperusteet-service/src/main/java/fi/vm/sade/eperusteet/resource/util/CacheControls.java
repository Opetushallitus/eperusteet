package fi.vm.sade.eperusteet.resource.util;

final class CacheControls {

    private CacheControls() {
    }


    public static final String PRIVATE_NOT_CACHEABLE = "private,no-cache";
    public static final String PUBLIC_NOT_CACHEABLE = "no-cache";

    private static final String PRIVATE_CACHEABLE = "private,max-age=";
    private static final String PUBLIC_CACHEABLE = "max-age=";

    public static String buildCacheControl(CacheControl cc) {
        if (cc.nocache()) {
            if (cc.nonpublic()) {
                return PRIVATE_NOT_CACHEABLE;
            } else {
                return PUBLIC_NOT_CACHEABLE;
            }
        } else {
            if (cc.nonpublic()) {
                return PRIVATE_CACHEABLE + cc.age();
            } else {
                return PUBLIC_CACHEABLE + cc.age();
            }
        }

    }
}
