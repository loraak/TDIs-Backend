package com.tdis.tramites.ia;

import com.tdis.common.dto.ActividadDTO;
import com.tdis.tramites.entity.Solicitud;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** IA desactivada: todas las evidencias las revisa el administrador. */
@Component
@ConditionalOnProperty(name = "tdis.ia.proveedor", havingValue = "ninguno", matchIfMissing = true)
public class SinIAAnalizador implements AnalizadorEvidencia {

    @Override
    public ResultadoIA analizar(ActividadDTO actividad, Solicitud solicitud, byte[] imagen) {
        return ResultadoIA.sinAnalisis("El analisis con IA no esta activado.");
    }
}
