package org.dcsa.ebl.model.transferobjects;

import org.dcsa.core.model.GetId;

public interface SetId<I> extends GetId<I> {
    void setId(I id);
}
