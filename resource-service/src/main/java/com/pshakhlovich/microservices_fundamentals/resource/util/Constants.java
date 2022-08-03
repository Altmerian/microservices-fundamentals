package com.pshakhlovich.microservices_fundamentals.resource.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

    public static final String AUDIO_CONTENT_TYPE = "audio/mpeg";

    public static final String EMULATE_TRANSIENT_ERROR_ENV_VARIABLE = "EMULATE_TRANSIENT_ERROR";
}
