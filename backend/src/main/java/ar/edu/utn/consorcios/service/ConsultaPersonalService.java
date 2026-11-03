
package ar.edu.utn.consorcios.service;

import ar.edu.utn.consorcios.dto.MiLiquidacionResponse;
import ar.edu.utn.consorcios.entity.*;
import ar.edu.utn.consorcios.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
public class ConsultaPersonalService {

    private final UsuarioRepository usuarios;
    private final VinculacionUnidadRepository vinculaciones;
    private final LiquidacionUnidadRepository detalles;

    public ConsultaPersonalService(
            UsuarioRepository usuarios,
            VinculacionUnidadRepository vinculaciones,
            LiquidacionUnidadRepository detalles) {

        this.usuarios = usuarios;
        this.vinculaciones = vinculaciones;
        this.detalles = detalles;
    }

    @Transactional(readOnly = true)
    public List<MiLiquidacionResponse> misLiquidaciones(
            Authentication auth) {

        // Identificamos al usuario autenticado.
        Usuario usuario = usuarios
                .findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuario no encontrado"
                ));

        if (!usuario.isActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Usuario inactivo"
            );
        }

        if (usuario.getRol() != Rol.PROPIETARIO
                && usuario.getRol() != Rol.INQUILINO) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Rol no autorizado"
            );
        }

        if (usuario.getPersona() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El usuario no tiene una persona asociada"
            );
        }

        // Obtenemos las vinculaciones de esa persona.
        List<VinculacionUnidad> relaciones =
                vinculaciones.findByPersonaId(
                        usuario.getPersona().getId()
                );

        return relaciones.stream()
                // La vinculación debe coincidir con el rol.
                .filter(v -> v.getTipo() ==
                        (usuario.getRol() == Rol.PROPIETARIO
                                ? TipoVinculo.PROPIETARIO
                                : TipoVinculo.INQUILINO))

                .flatMap(v -> detalles
                        .findByUnidadFuncionalId(
                                v.getUnidadFuncional().getId()
                        )
                        .stream()
                        // Solo liquidaciones cerradas.
                        .filter(d -> d.getLiquidacion().getEstado()
                                == EstadoLiquidacion.CERRADA)

                        // El período liquidado debe estar
                        // dentro de la vigencia de la vinculación.
                        .filter(d -> {
                            java.time.YearMonth mes =
                                    java.time.YearMonth.parse(
                                            d.getLiquidacion().getPeriodo()
                                    );

                            return !v.getFechaInicio()
                                    .isAfter(mes.atEndOfMonth())
                                    && (v.getFechaFin() == null
                                    || !v.getFechaFin()
                                    .isBefore(mes.atDay(1)));
                        })

                        // Los inquilinos solo ven lo autorizado.
                        .filter(d -> usuario.getRol() == Rol.PROPIETARIO
                                || d.isVisibleParaInquilino())

                        .map(d -> new MiLiquidacionResponse(
                                d.getLiquidacion().getId(),
                                d.getUnidadFuncional().getId(),
                                d.getUnidadFuncional().getIdentificador(),
                                d.getLiquidacion().getPeriodo(),
                                d.getLiquidacion().getEstado(),
                                d.getImporte(),
                                d.getEstadoPago(),
                                d.getFechaPago()
                        ))
                )
                .distinct()
                .sorted(Comparator.comparing(
                        MiLiquidacionResponse::periodo
                ).reversed())
                .toList();
    }
}
