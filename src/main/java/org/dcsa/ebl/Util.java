package org.dcsa.ebl;

import org.dcsa.core.exception.UpdateException;

import java.util.function.Consumer;
import java.util.function.Function;

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

    public static <T> Consumer<T> acceptAny() {
        return (t) -> {};
    }

    public static <T, I> Consumer<T> fieldMustEqual(String objectName, String fieldName, Function<T, I> getter,
                                                    I expectedValue, boolean allowNull) {
        return t -> {
            I value = getter.apply(t);
            if (value == null) {
                if (allowNull) {
                    return;
                }
            } else if (expectedValue.equals(value)) {
                return;
            }
            if (allowNull) {
                throw new UpdateException("The field " + fieldName + " on " + objectName
                        + " must either be omitted or set to " + expectedValue);
            }
            throw new UpdateException("The field " + fieldName + " on " + objectName + " must be " + expectedValue);
        };
    }

    private Util() {}
}
