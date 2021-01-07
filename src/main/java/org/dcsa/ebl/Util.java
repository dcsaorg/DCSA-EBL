package org.dcsa.ebl;

public class Util {

    /* For use with ".buffer(...).concatMap(service::createOrUpdateOrDeleteAll), etc. where the underlying
     * operation uses a variant "WHERE foo IN (LIST)".
     *
     * A higher number means fewer queries but after a certain size postgres performance will degrade.
     * Plus a higher number will also require more memory (java-side) as we build up a list of items.
     *
     * The number should be sufficient to bundle most trivial things into a single query without hitting
     * performance issues.
     */
    public static final int SQL_LIST_BUFFER_SIZE = 70;

    private Util() {}
}
