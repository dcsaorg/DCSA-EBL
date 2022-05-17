package org.dcsa.ebl.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.ebl.model.transferobjects.ApproveTransportDocumentRequestTO;
import org.dcsa.ebl.model.transferobjects.TransportDocumentRefStatusTO;
import org.dcsa.ebl.model.transferobjects.TransportDocumentTO;
import org.dcsa.ebl.service.TransportDocumentService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(
    value = "unofficial/reset-transport-document",
    produces = {MediaType.APPLICATION_JSON_VALUE})
@Validated
public class ResetTransportDocumentController {

  private final TransportDocumentService transportDocumentService;

  @PostMapping(path = "/{transportDocumentId}")
  public Mono<Void> findById(
      @PathVariable UUID transportDocumentId) {
      return transportDocumentService.resetTransportDocument(transportDocumentId);
  }
}
