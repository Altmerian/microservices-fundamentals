package com.pshakhlovich.microservices_fundamentals.resource.processor.infrastructure.song;

import com.pshakhlovich.microservices_fundamentals.resource.processor.domain.SongMetadata;

public interface SongClient {
  Integer storeSongMetadata(SongMetadata songMetadata);
}
