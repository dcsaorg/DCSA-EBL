package org.dcsa.ebl.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.dcsa.ebl.model.ActiveReeferSettings;
import org.dcsa.ebl.repository.ActiveReeferSettingsRepository;
import org.dcsa.ebl.service.ActiveReeferSettingsService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ActiveReeferSettingsServiceImpl extends ExtendedBaseServiceImpl<ActiveReeferSettingsRepository, ActiveReeferSettings, UUID> implements ActiveReeferSettingsService {
    private final ActiveReeferSettingsRepository activeReeferSettingsRepository;

    @Override
    public ActiveReeferSettingsRepository getRepository() {
        return activeReeferSettingsRepository;
    }

    @Override
    public Class<ActiveReeferSettings> getModelClass() {
        return ActiveReeferSettings.class;
    }
}
