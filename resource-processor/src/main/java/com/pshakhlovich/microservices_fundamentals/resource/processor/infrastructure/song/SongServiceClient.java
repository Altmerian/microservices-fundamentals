package com.pshakhlovich.microservices_fundamentals.resource.processor.infrastructure.song;

import com.pshakhlovich.microservices_fundamentals.resource.processor.domain.IdWrapper;
import com.pshakhlovich.microservices_fundamentals.resource.processor.domain.SongMetadata;

public interface SongServiceClient {
    IdWrapper<Integer> storeSongMetadata(SongMetadata songMetadata);
}
