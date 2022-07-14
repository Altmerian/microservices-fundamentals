package com.pshakhlovich.microservices_fundamentals.resource.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NullableValueSetter {

    public static <N, T> T checkNonNullAndApply(@Nullable N nullable, Function<N, T> valueSupplier) {
        return nullable != null ? valueSupplier.apply(nullable) : null;
    }
}
