package com.estacionnexo.shared;

import java.util.UUID;

public final class Comprobante {
    private Comprobante() {}

    public static String codigo(String prefijo) {
        return prefijo + UUID.randomUUID().toString().replace("-", "").substring(0, 12 - prefijo.length());
    }
}
