
package ar.edu.utn.consorcios.service;

import ar.edu.utn.consorcios.dto.*;
import ar.edu.utn.consorcios.entity.*;
import ar.edu.utn.consorcios.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class LiquidacionService {

    private final LiquidacionRepository liquidaciones;
    private final LiquidacionUnidadRepository detalles;
    private final UnidadFuncionalRepository unidades;
    private final GastoRepository gastos;
    private final ConsorcioService consorcioService;

    public LiquidacionService(
            LiquidacionRepository liquidaciones,
            LiquidacionUnidadRepository detalles,
            UnidadFuncionalRepository unidades,
            GastoRepository gastos,
            ConsorcioService consorcioService) {

        this.liquidaciones = liquidaciones;
        this.detalles = detalles;
        this.unidades = unidades;
        this.gastos = gastos;
        this.consorcioService = consorcioService;
    }

    @Transactional
    public LiquidacionResponse generar(
            Authentication auth,
            UUID consorcioId,
            LiquidacionRequest request) {

        Consorcio consorcio =
                consorcioService.exigirAcceso(auth, consorcioId);

        String periodo = request.periodo();

        if (liquidaciones.findByConsorcioIdAndPeriodo(
                consorcioId, periodo).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una liquidación para este período"
            );
        }

        YearMonth mes = YearMonth.parse(periodo);

        List<Gasto> gastosDelMes =
                gastos.findByConsorcioIdAndFechaBetween(
                        consorcioId,
                        mes.atDay(1),
                        mes.atEndOfMonth()
                );

        if (gastosDelMes.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No hay gastos registrados en el período"
            );
        }

        BigDecimal total = gastosDelMes.stream()
                .map(Gasto::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        List<UnidadFuncional> listaUnidades =
                unidades.findByConsorcioIdOrderByIdentificador(
                                consorcioId
                        ).stream()
                        .sorted(Comparator.comparing(
                                u -> u.getId().toString()
                        ))
                        .toList();

        if (listaUnidades.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El consorcio no tiene unidades"
            );
        }

        BigDecimal divisor = consorcio.getTotalCoeficientes();

        if (divisor == null ||
                divisor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Total de coeficientes inválido"
            );
        }

        BigDecimal sumaCoeficientes = listaUnidades.stream()
                .map(UnidadFuncional::getCoeficientePropiedad)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sumaCoeficientes.compareTo(divisor) != 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Los coeficientes de las unidades no suman el total configurado"
            );
        }

        Liquidacion liquidacion = new Liquidacion();
        liquidacion.setConsorcio(consorcio);
        liquidacion.setPeriodo(periodo);
        liquidacion.setEstado(EstadoLiquidacion.ABIERTA);

        liquidacion = liquidaciones.save(liquidacion);

        BigDecimal acumulado = BigDecimal.ZERO;

        for (int i = 0; i < listaUnidades.size(); i++) {

            UnidadFuncional unidad = listaUnidades.get(i);

            BigDecimal importe;

            if (i == listaUnidades.size() - 1) {
                importe = total.subtract(acumulado);
            } else {
                importe = total
                        .multiply(unidad.getCoeficientePropiedad())
                        .divide(divisor, 2, RoundingMode.HALF_UP);

                acumulado = acumulado.add(importe);
            }

            LiquidacionUnidad detalle = new LiquidacionUnidad();

            detalle.setConsorcio(consorcio);
            detalle.setLiquidacion(liquidacion);
            detalle.setUnidadFuncional(unidad);
            detalle.setImporte(importe);
            detalle.setEstadoPago(EstadoPago.PENDIENTE);
            detalle.setVisibleParaInquilino(false);

            detalles.save(detalle);
        }

        return new LiquidacionResponse(
                liquidacion.getId(),
                consorcioId,
                periodo,
                liquidacion.getEstado(),
                total,
                listaUnidades.size()
        );
    }

    @Transactional(readOnly = true)
    public List<LiquidacionResponse> listar(
            Authentication auth,
            UUID consorcioId) {

        consorcioService.exigirAcceso(auth, consorcioId);

        return liquidaciones
                .findByConsorcioIdOrderByPeriodoDesc(consorcioId)
                .stream()
                .map(l -> {
                    List<LiquidacionUnidad> lista =
                            detalles.findByLiquidacionId(l.getId());

                    BigDecimal total = lista.stream()
                            .map(LiquidacionUnidad::getImporte)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new LiquidacionResponse(
                            l.getId(),
                            consorcioId,
                            l.getPeriodo(),
                            l.getEstado(),
                            total,
                            lista.size()
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LiquidacionUnidadResponse> consultarDetalle(
            Authentication auth,
            UUID consorcioId,
            UUID liquidacionId) {

        buscarLiquidacion(auth, consorcioId, liquidacionId);

        return detalles.findByLiquidacionId(liquidacionId)
                .stream()
                .map(d -> new LiquidacionUnidadResponse(
                        d.getId(),
                        d.getUnidadFuncional().getId(),
                        d.getUnidadFuncional().getIdentificador(),
                        d.getImporte(),
                        d.getEstadoPago(),
                        d.getFechaPago(),
                        d.isVisibleParaInquilino()
                ))
                .toList();
    }

    @Transactional
    public LiquidacionResponse cerrar(
            Authentication auth,
            UUID consorcioId,
            UUID liquidacionId) {

        Liquidacion liquidacion =
                buscarLiquidacion(auth, consorcioId, liquidacionId);

        if (liquidacion.getEstado() == EstadoLiquidacion.CERRADA) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La liquidación ya está cerrada"
            );
        }

        liquidacion.cerrar();
        liquidaciones.save(liquidacion);

        List<LiquidacionUnidad> lista =
                detalles.findByLiquidacionId(liquidacionId);

        BigDecimal total = lista.stream()
                .map(LiquidacionUnidad::getImporte)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new LiquidacionResponse(
                liquidacion.getId(),
                consorcioId,
                liquidacion.getPeriodo(),
                liquidacion.getEstado(),
                total,
                lista.size()
        );
    }

    private Liquidacion buscarLiquidacion(
            Authentication auth,
            UUID consorcioId,
            UUID liquidacionId) {

        consorcioService.exigirAcceso(auth, consorcioId);

        Liquidacion liquidacion =
                liquidaciones.findById(liquidacionId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Liquidación inexistente"
                        ));

        if (!liquidacion.getConsorcio().getId()
                .equals(consorcioId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Liquidación inexistente en este consorcio"
            );
        }

        return liquidacion;
    }
}
