package org.dcsa.ebl.model;

import org.dcsa.ebl.model.enums.PartyType;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

public class Party {
    private PartyType partyType;

    @Size(max = 100)
    private String partyName;

    @Email
    @Size(max = 250)
    private String email;

    @Size(max = 20)
    private String phoneNumber;

    @Size(max = 20)
    private String fax;

    @Size(max = 20)
    private String taxReference;
}
