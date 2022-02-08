package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.events.repository.ActiveReeferSettingsRepository;
import org.dcsa.ebl.service.ActiveReeferSettingsService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ActiveReeferSettingsServiceImpl implements ActiveReeferSettingsService {
    private final ActiveReeferSettingsRepository activeReeferSettingsRepository;

    public ActiveReeferSettingsRepository getRepository() {
        return activeReeferSettingsRepository;
    }
}
