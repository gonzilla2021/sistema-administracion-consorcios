
package ar.edu.utn.consorcios.service;

import ar.edu.utn.consorcios.dto.*;
import ar.edu.utn.consorcios.entity.*;
import ar.edu.utn.consorcios.repository.LiquidacionUnidadRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class PagoService {

    private final LiquidacionUnidadRepository detalles;
    private final ConsorcioService consorcioService;

    public PagoService(
            LiquidacionUnidadRepository detalles,
            ConsorcioService consorcioService) {

        this.detalles = detalles;
        this.consorcioService = consorcioService;
    }

    // Consulta los pagos de un consorcio.
    @Transactional(readOnly = true)
    public List<PagoResponse> listar(
            Authentication auth,
            UUID consorcioId) {

        consorcioService.exigirAcceso(auth, consorcioId);

        return detalles.findByConsorcioId(consorcioId)
                .stream()
                .map(this::convertir)
                .toList();
    }

    // Marca una liquidación de unidad como pagada.
    @Transactional
    public PagoResponse registrar(
            Authentication auth,
            UUID consorcioId,
            UUID detalleId,
            PagoRequest request) {

        consorcioService.exigirAcceso(auth, consorcioId);

        LiquidacionUnidad detalle =
                buscarDetalle(consorcioId, detalleId);

        if (detalle.getLiquidacion().getEstado()
                != EstadoLiquidacion.CERRADA) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La liquidación debe estar cerrada para registrar el pago"
            );
        }

        if (detalle.getEstadoPago() == EstadoPago.PAGADO) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El pago ya fue registrado"
            );
        }

        detalle.marcarPagada(request.fechaPago());

        return convertir(detalles.save(detalle));
    }

    // Revierte un pago registrado por error.
    @Transactional
    public PagoResponse revertir(
            Authentication auth,
            UUID consorcioId,
            UUID detalleId) {

        consorcioService.exigirAcceso(auth, consorcioId);

        LiquidacionUnidad detalle =
                buscarDetalle(consorcioId, detalleId);

        if (detalle.getEstadoPago() == EstadoPago.PENDIENTE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La liquidación ya está pendiente"
            );
        }

        detalle.setEstadoPago(EstadoPago.PENDIENTE);
        detalle.setFechaPago(null);

        return convertir(detalles.save(detalle));
    }

    // Verifica que el registro corresponda al consorcio.
    private LiquidacionUnidad buscarDetalle(
            UUID consorcioId,
            UUID detalleId) {

        LiquidacionUnidad detalle =
                detalles.findById(detalleId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Registro de pago inexistente"
                        ));

        if (!detalle.getConsorcio().getId().equals(consorcioId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Registro inexistente en este consorcio"
            );
        }

        return detalle;
    }

    // Convierte la entidad al DTO de respuesta.
    private PagoResponse convertir(LiquidacionUnidad detalle) {

        return new PagoResponse(
                detalle.getId(),
                detalle.getLiquidacion().getId(),
                detalle.getUnidadFuncional().getId(),
                detalle.getUnidadFuncional().getIdentificador(),
                detalle.getImporte(),
                detalle.getEstadoPago(),
                detalle.getFechaPago()
        );
    }
}
