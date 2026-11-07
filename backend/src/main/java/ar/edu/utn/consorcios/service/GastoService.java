
package ar.edu.utn.consorcios.service;

import ar.edu.utn.consorcios.dto.*;
import ar.edu.utn.consorcios.entity.*;
import ar.edu.utn.consorcios.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
public class GastoService {

    private final GastoRepository gastos;
    private final LiquidacionRepository liquidaciones;
    private final ConsorcioService consorcioService;

    public GastoService(
            GastoRepository gastos,
            LiquidacionRepository liquidaciones,
            ConsorcioService consorcioService) {

        this.gastos = gastos;
        this.liquidaciones = liquidaciones;
        this.consorcioService = consorcioService;
    }

    // Comprueba si la liquidación del mes está cerrada.
    private void exigirPeriodoEditable(
            UUID consorcioId,
            LocalDate fecha) {

        String periodo = YearMonth.from(fecha).toString();

        liquidaciones.findByConsorcioIdAndPeriodo(
                consorcioId, periodo
        ).ifPresent(liquidacion -> {

            if (liquidacion.getEstado()
                    == EstadoLiquidacion.CERRADA) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "La liquidación del período está cerrada"
                );
            }
        });
    }

    // Registra un gasto.
    @Transactional
    public GastoResponse crear(
            Authentication auth,
            UUID consorcioId,
            GastoRequest request) {

        Consorcio consorcio =
                consorcioService.exigirAcceso(auth, consorcioId);

        exigirPeriodoEditable(consorcioId, request.fecha());

        Gasto gasto = new Gasto();

        gasto.setConsorcio(consorcio);
        aplicarDatos(gasto, request);

        return convertir(gastos.save(gasto));
    }

    // Lista los gastos de un consorcio.
    @Transactional(readOnly = true)
    public List<GastoResponse> listar(
            Authentication auth,
            UUID consorcioId) {

        consorcioService.exigirAcceso(auth, consorcioId);

        return gastos.findByConsorcioId(consorcioId)
                .stream()
                .map(this::convertir)
                .toList();
    }

    // Modifica un gasto existente.
    @Transactional
    public GastoResponse actualizar(
            Authentication auth,
            UUID consorcioId,
            UUID gastoId,
            GastoRequest request) {

        consorcioService.exigirAcceso(auth, consorcioId);

        Gasto gasto = buscarGasto(consorcioId, gastoId);

        // Validamos tanto el mes original como el nuevo.
        exigirPeriodoEditable(consorcioId, gasto.getFecha());
        exigirPeriodoEditable(consorcioId, request.fecha());

        aplicarDatos(gasto, request);

        return convertir(gastos.save(gasto));
    }

    // Elimina un gasto si su período no está cerrado.
    @Transactional
    public void eliminar(
            Authentication auth,
            UUID consorcioId,
            UUID gastoId) {

        consorcioService.exigirAcceso(auth, consorcioId);

        Gasto gasto = buscarGasto(consorcioId, gastoId);

        exigirPeriodoEditable(consorcioId, gasto.getFecha());

        gastos.delete(gasto);
    }

    // Busca un gasto y comprueba su consorcio.
    private Gasto buscarGasto(UUID consorcioId, UUID gastoId) {

        Gasto gasto = gastos.findById(gastoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Gasto inexistente"
                ));

        if (!gasto.getConsorcio().getId().equals(consorcioId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Gasto inexistente en este consorcio"
            );
        }

        return gasto;
    }

    // Copia y normaliza los datos recibidos.
    private void aplicarDatos(Gasto gasto, GastoRequest request) {

        gasto.setDescripcion(request.descripcion().trim());
        gasto.setMonto(request.monto().setScale(
                2, RoundingMode.HALF_UP
        ));
        gasto.setTipo(request.tipo());
        gasto.setFecha(request.fecha());
        gasto.setComprobante(request.comprobante());
    }

    // Transforma la entidad en un DTO.
    private GastoResponse convertir(Gasto gasto) {

        return new GastoResponse(
                gasto.getId(),
                gasto.getConsorcio().getId(),
                gasto.getDescripcion(),
                gasto.getMonto(),
                gasto.getTipo(),
                gasto.getFecha(),
                gasto.getComprobante()
        );
    }
}
