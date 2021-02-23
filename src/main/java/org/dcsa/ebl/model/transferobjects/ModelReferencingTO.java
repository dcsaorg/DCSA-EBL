package org.dcsa.ebl.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.dcsa.core.model.GetId;

public interface ModelReferencingTO<M, I> extends GetId<I> {

    /**
     * @return true if the TO is just an ID reference to a model.
     * If this returns true, then {@link #getId()} must return not null.
     * At the same time, if this returns true, then {@link #isEqualsToModel(Object)}
     * will <b>not</b> be called.
     */
    @JsonIgnore
    boolean isSolelyReferenceToModel();

    /**
     * @param m A model instance with the ID matching the one returned
     *          by {@link #getId()} (when it does not return null).
     * @return Should return true if this instance is (semantically)
     * equal to the provided model (a la {@link #equals(Object)} and
     * false other wise.
     */
    boolean isEqualsToModel(M m);
}
