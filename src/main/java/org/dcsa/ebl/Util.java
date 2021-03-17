package org.dcsa.ebl;

import org.dcsa.core.exception.UpdateException;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.model.transferobjects.ModelReferencingTO;
import org.dcsa.ebl.model.transferobjects.SetId;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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

    public static <T, I> void checkForDuplicates(Iterable<T> objects, Function<T, I> idMapper, String name) {
        Set<I> usedIds = new HashSet<>();
        for (T t : objects) {
            I id = idMapper.apply(t);
            if (id == null) {
                continue;
            }
            if (!usedIds.add(id)) {
                throw new UpdateException("Duplicate ID " + id + " (" + name + "):  The ID must occur at most once.");
            }
        }

    }

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

    public static <TO extends ModelReferencingTO<M, ID>, M extends GetId<ID>, ID> Mono<M> resolveModelReference(TO instanceTO, Function<ID, Mono<M>> findByID, Function<TO, Mono<M>> create, String entityName) {
        ID id = Objects.requireNonNull(instanceTO).getId();
        if (id != null) {
            return findByID.apply(id)
                    .doOnNext(m -> {
                        if (!instanceTO.isSolelyReferenceToModel() && !instanceTO.isEqualsToModel(m)) {
                            throw new UpdateException(entityName + " with id " + id
                                    + " exists but has a different content. Remove the ID field to"
                                    + " create a new instance or provide an update");
                        }
                    });
        } else {
            return create.apply(instanceTO)
                    .doOnNext(m -> {
                        if (m instanceof SetId) {
                            @SuppressWarnings({"rawtypes", "unchecked"})
                            SetId<ID> s = ((SetId)m);
                            s.setId(m.getId());
                        }
                    });
        }
    }

    public static <T extends SetId<I>, I> boolean containsOnlyID(T model, Supplier<T> constructor) {
        I id = model.getId();
        if (id != null) {
            T t = constructor.get();
            if (t.getClass() != model.getClass()) {
                throw new IllegalArgumentException("Logic error: this method assumes that the class is the same");
            }
            t.setId(id);
            return model.equals(t);
        }
        return false;
    }

    private Util() {}
}
