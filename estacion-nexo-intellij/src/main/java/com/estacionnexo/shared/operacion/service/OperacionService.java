package com.estacionnexo.shared.operacion.service;

import com.estacionnexo.shared.operacion.dto.OperacionSolicitud;

public interface OperacionService {
    long comprar(long usuario, OperacionSolicitud solicitud);
    long vender(long usuario, OperacionSolicitud solicitud);
}
