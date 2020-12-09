package org.dcsa.ebl.controller;

public abstract class AbstractTOController<S> {

    public abstract S getService();
    public abstract String getType();

}
