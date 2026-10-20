package com.portal.residencial.casos_service.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portal.residencial.casos_service.dto.reporte.FiltroReporteCasos;
import com.portal.residencial.casos_service.dto.reporte.ReporteCasosResponse;
import com.portal.residencial.casos_service.dto.reporte.ReporteCasosResponse.CasoEstancado;
import com.portal.residencial.casos_service.dto.reporte.ReporteCasosResponse.Conteo;
import com.portal.residencial.casos_service.dto.reporte.ReporteCasosResponse.DesempenoAnalista;
import com.portal.residencial.casos_service.dto.reporte.ReporteCasosResponse.Resumen;
import com.portal.residencial.casos_service.dto.reporte.ReporteCasosResponse.TendenciaMensual;
import com.portal.residencial.casos_service.dto.reporte.ReporteCasosResponse.TiemposAtencion;
import com.portal.residencial.casos_service.exception.BusinessException;
import com.portal.residencial.casos_service.exportacion.ExportadorExcel;
import com.portal.residencial.casos_service.exportacion.ExportadorPdf;
import com.portal.residencial.casos_service.exportacion.TablaExportable;
import com.portal.residencial.casos_service.model.Caso;
import com.portal.residencial.casos_service.model.EstadoCaso;
import com.portal.residencial.casos_service.model.SeguimientoCaso;
import com.portal.residencial.casos_service.model.TipoSeguimiento;
import com.portal.residencial.casos_service.repository.CasoRepository;
import com.portal.residencial.casos_service.repository.SeguimientoCasoRepository;

import lombok.RequiredArgsConstructor;

/* REPORTE DE CASOS */
@Service
@RequiredArgsConstructor
public class ReporteCasoService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int MAXIMO_MESES_TENDENCIA = 24;
    private static final int MAXIMO_TIPOS = 10;

    private final CasoRepository casoRepository;
    private final SeguimientoCasoRepository seguimientoRepository;
    private final ExportadorExcel exportadorExcel;
    private final ExportadorPdf exportadorPdf;

    /* ARCHIVO EXPORTADO */
    public record ArchivoReporte(byte[] contenido, String nombre, String tipoMime) {}

    @Transactional(readOnly = true)
    public ReporteCasosResponse generar(FiltroReporteCasos filtro) {
        List<Caso> casos = filtrar(filtro);
        Map<Long, Hitos> hitos = calcularHitos(casos);

        return new ReporteCasosResponse(
                resumen(casos),
                porEstado(casos),
                agrupar(casos, c -> c.getCategoria().getNombre(), Integer.MAX_VALUE),
                agrupar(casos, c -> c.getTipoIncidente().getNombre(), MAXIMO_TIPOS),
                porAnalista(casos, hitos),
                tiempos(hitos.values()),
                tendencia(casos, filtro),
                estancados(casos, filtro.diasEstancadoEfectivo()));
    }

    @Transactional(readOnly = true)
    public ArchivoReporte exportar(FiltroReporteCasos filtro, String formato) {
        ReporteCasosResponse reporte = generar(filtro);
        List<Caso> casos = filtrar(filtro);

        String titulo = "Reporte de casos";
        List<String> encabezado = describirFiltros(filtro);
        List<TablaExportable> tablas = List.of(
                tablaResumen(reporte),
                tablaAnalistas(reporte),
                tablaDetalle(casos),
                tablaEstancados(reporte));

        String nombreBase = "reporte-casos-" + filtro.desde() + "-a-" + filtro.hasta();

        return switch (formato == null ? "" : formato.toLowerCase()) {
            case "excel" -> new ArchivoReporte(exportadorExcel.generar(titulo, encabezado, tablas),
                    nombreBase + ".xlsx", ExportadorExcel.TIPO_MIME);
            case "pdf" -> new ArchivoReporte(exportadorPdf.generar(titulo, encabezado, tablas),
                    nombreBase + ".pdf", ExportadorPdf.TIPO_MIME);
            default -> throw new BusinessException("Formato no soportado. Use 'excel' o 'pdf'.", HttpStatus.BAD_REQUEST);
        };
    }

    /* FILTROS */

    private List<Caso> filtrar(FiltroReporteCasos filtro) {
        if (filtro.hasta().isBefore(filtro.desde())) {
            throw new BusinessException("La fecha final no puede ser anterior a la inicial.", HttpStatus.BAD_REQUEST);
        }

        if (filtro.estado() != null && !filtro.estado().isBlank()
                && Arrays.stream(EstadoCaso.values()).noneMatch(e -> e.name().equals(filtro.estado()))) {
            throw new BusinessException("Estado no válido: " + filtro.estado(), HttpStatus.BAD_REQUEST);
        }

        Specification<Caso> spec = (root, query, cb) -> {
            var condiciones = new ArrayList<jakarta.persistence.criteria.Predicate>();
            condiciones.add(cb.greaterThanOrEqualTo(root.get("fechaRegistro"), filtro.desde().atStartOfDay()));
            condiciones.add(cb.lessThan(root.get("fechaRegistro"), filtro.hasta().plusDays(1).atStartOfDay()));

            if (filtro.estado() != null && !filtro.estado().isBlank()) {
                condiciones.add(cb.equal(root.get("estado"), filtro.estado()));
            }
            if (filtro.idCategoria() != null) {
                condiciones.add(cb.equal(root.get("categoria").get("id"), filtro.idCategoria()));
            }
            if (filtro.idAnalista() != null) {
                condiciones.add(cb.equal(root.get("idAnalista"), filtro.idAnalista()));
            }

            return cb.and(condiciones.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };

        return casoRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "fechaRegistro"));
    }

    /* INDICADORES */

    private Resumen resumen(List<Caso> casos) {
        long finalizados = contar(casos, EstadoCaso.FINALIZADO);
        long rechazados = contar(casos, EstadoCaso.RECHAZADO);
        long sinAsignar = casos.stream().filter(c -> c.getIdAnalista() == null).count();
        long decididos = finalizados + rechazados;

        return new Resumen(
                casos.size(),
                casos.size() - decididos,
                finalizados,
                rechazados,
                sinAsignar,
                decididos == 0 ? null : redondear(rechazados * 100.0 / decididos));
    }

    /* CASOS POR ESTADO */
    private List<Conteo> porEstado(List<Caso> casos) {
        return Arrays.stream(EstadoCaso.values())
                .map(estado -> new Conteo(estado.name(), estado.getDescripcion(), contar(casos, estado)))
                .toList();
    }

    private List<Conteo> agrupar(List<Caso> casos, Function<Caso, String> clave, int maximo) {
        return casos.stream()
                .collect(Collectors.groupingBy(clave, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .limit(maximo)
                .map(e -> new Conteo(e.getKey(), e.getKey(), e.getValue()))
                .toList();
    }

    private List<DesempenoAnalista> porAnalista(List<Caso> casos, Map<Long, Hitos> hitos) {
        return casos.stream()
                .filter(c -> c.getIdAnalista() != null)
                .collect(Collectors.groupingBy(Caso::getIdAnalista, LinkedHashMap::new, Collectors.toList()))
                .entrySet().stream()
                .map(e -> {
                    List<Caso> delAnalista = e.getValue();
                    long finalizados = contar(delAnalista, EstadoCaso.FINALIZADO);
                    long rechazados = contar(delAnalista, EstadoCaso.RECHAZADO);

                    Double promedio = promedio(delAnalista.stream()
                            .filter(c -> estadoDe(c) == EstadoCaso.FINALIZADO)
                            .map(c -> horas(c.getFechaRegistro(), hitos.get(c.getId()).cierre()))
                            .toList());

                    return new DesempenoAnalista(
                            e.getKey(),
                            delAnalista.get(0).getNombreAnalista(),
                            delAnalista.size(),
                            delAnalista.size() - finalizados - rechazados,
                            finalizados,
                            rechazados,
                            promedio);
                })
                .sorted(Comparator.comparingLong(DesempenoAnalista::asignados).reversed())
                .toList();
    }

    private TiemposAtencion tiempos(java.util.Collection<Hitos> hitos) {
        return new TiemposAtencion(
                promedio(hitos.stream().map(h -> horas(h.registro(), h.asignacion())).toList()),
                promedio(hitos.stream().map(h -> horas(h.asignacion(), h.solicitud())).toList()),
                promedio(hitos.stream().map(h -> horas(h.solicitud(), h.decision())).toList()),
                promedio(hitos.stream().map(h -> horas(h.registro(), h.cierre())).toList()));
    }

    /* CASOS REGISTRADOS Y CERRADOS POR MES */
    private List<TendenciaMensual> tendencia(List<Caso> casos, FiltroReporteCasos filtro) {
        YearMonth inicio = YearMonth.from(filtro.desde());
        YearMonth fin = YearMonth.from(filtro.hasta());

        if (inicio.plusMonths(MAXIMO_MESES_TENDENCIA).isBefore(fin)) {
            inicio = fin.minusMonths(MAXIMO_MESES_TENDENCIA - 1);
        }

        Map<YearMonth, Long> registrados = casos.stream()
                .collect(Collectors.groupingBy(c -> YearMonth.from(c.getFechaRegistro()), Collectors.counting()));
        Map<YearMonth, Long> cerrados = casos.stream()
                .filter(c -> c.getFechaCierre() != null)
                .collect(Collectors.groupingBy(c -> YearMonth.from(c.getFechaCierre()), Collectors.counting()));

        List<TendenciaMensual> tendencia = new ArrayList<>();
        for (YearMonth mes = inicio; !mes.isAfter(fin); mes = mes.plusMonths(1)) {
            tendencia.add(new TendenciaMensual(mes.toString(),
                    registrados.getOrDefault(mes, 0L), cerrados.getOrDefault(mes, 0L)));
        }
        return tendencia;
    }

    /* CASOS SIN MOVIMIENTO */
    private List<CasoEstancado> estancados(List<Caso> casos, int dias) {
        LocalDateTime limite = LocalDateTime.now().minusDays(dias);

        return casos.stream()
                .filter(c -> !estadoDe(c).esFinal())
                .filter(c -> c.getFechaActualizacion().isBefore(limite))
                .sorted(Comparator.comparing(Caso::getFechaActualizacion))
                .map(c -> new CasoEstancado(
                        c.getId(),
                        c.getNumeroCaso(),
                        c.getTipoIncidente().getNombre(),
                        c.getEstado(),
                        estadoDe(c).getDescripcion(),
                        c.getNombreAnalista(),
                        c.getFechaActualizacion(),
                        ChronoUnit.DAYS.between(c.getFechaActualizacion(), LocalDateTime.now())))
                .toList();
    }

    /* FECHAS DE CADA ETAPA */

    /* FECHAS DE CADA ETAPA DEL CASO */
    private record Hitos(LocalDateTime registro, LocalDateTime asignacion, LocalDateTime solicitud,
            LocalDateTime decision, LocalDateTime cierre) {}

    private Map<Long, Hitos> calcularHitos(List<Caso> casos) {
        if (casos.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<SeguimientoCaso>> porCaso = seguimientoRepository
                .findByCaso_IdInOrderByFechaAscIdAsc(casos.stream().map(Caso::getId).toList()).stream()
                .collect(Collectors.groupingBy(s -> s.getCaso().getId()));

        return casos.stream().collect(Collectors.toMap(Caso::getId, caso -> {
            List<SeguimientoCaso> historial = porCaso.getOrDefault(caso.getId(), List.of());
            return new Hitos(
                    caso.getFechaRegistro(),
                    primero(historial, TipoSeguimiento.ASIGNACION),
                    primero(historial, TipoSeguimiento.SOLICITUD_AUTORIZACION),
                    primero(historial, TipoSeguimiento.AUTORIZACION, TipoSeguimiento.RECHAZO),
                    estadoDe(caso).esFinal() ? caso.getFechaCierre() : null);
        }));
    }

    private static LocalDateTime primero(List<SeguimientoCaso> historial, TipoSeguimiento... tipos) {
        List<String> nombres = Arrays.stream(tipos).map(Enum::name).toList();
        return historial.stream()
                .filter(s -> nombres.contains(s.getTipo()))
                .map(SeguimientoCaso::getFecha)
                .findFirst()
                .orElse(null);
    }

    /* TABLAS PARA EXPORTAR */

    private List<String> describirFiltros(FiltroReporteCasos filtro) {
        List<String> lineas = new ArrayList<>();
        lineas.add("Período (fecha de registro): " + filtro.desde().format(FECHA) + " al " + filtro.hasta().format(FECHA));
        if (filtro.estado() != null && !filtro.estado().isBlank()) {
            lineas.add("Estado: " + EstadoCaso.valueOf(filtro.estado()).getDescripcion());
        }
        if (filtro.idCategoria() != null) {
            lineas.add("Categoría (id): " + filtro.idCategoria());
        }
        if (filtro.idAnalista() != null) {
            lineas.add("Analista (id): " + filtro.idAnalista());
        }
        lineas.add("Generado: " + LocalDateTime.now().format(FECHA_HORA));
        return lineas;
    }

    private TablaExportable tablaResumen(ReporteCasosResponse r) {
        List<List<Object>> filas = new ArrayList<>();
        filas.add(List.of("Total de casos", r.resumen().total()));
        filas.add(List.of("Abiertos", r.resumen().abiertos()));
        filas.add(List.of("Finalizados", r.resumen().finalizados()));
        filas.add(List.of("Rechazados", r.resumen().rechazados()));
        filas.add(List.of("Sin asignar", r.resumen().sinAsignar()));
        filas.add(fila("% de rechazo (sobre decididos)", r.resumen().porcentajeRechazo()));
        filas.add(fila("Horas promedio: registro a asignación", r.tiempos().registroAAsignacion()));
        filas.add(fila("Horas promedio: asignación a solicitud de autorización", r.tiempos().asignacionASolicitud()));
        filas.add(fila("Horas promedio: solicitud a decisión del supervisor", r.tiempos().solicitudADecision()));
        filas.add(fila("Horas promedio: registro a cierre", r.tiempos().registroACierre()));
        r.porEstado().forEach(c -> filas.add(List.of("Estado: " + c.etiqueta(), c.cantidad())));
        r.porCategoria().forEach(c -> filas.add(List.of("Categoría: " + c.etiqueta(), c.cantidad())));
        return new TablaExportable("Resumen", List.of("Indicador", "Valor"), filas);
    }

    private TablaExportable tablaAnalistas(ReporteCasosResponse r) {
        List<List<Object>> filas = r.porAnalista().stream()
                .map(a -> fila(a.nombreAnalista(), a.asignados(), a.enCurso(), a.finalizados(), a.rechazados(),
                        a.promedioHorasResolucion()))
                .toList();
        return new TablaExportable("Por analista",
                List.of("Analista", "Asignados", "En curso", "Finalizados", "Rechazados", "Horas prom. resolución"), filas);
    }

    private TablaExportable tablaDetalle(List<Caso> casos) {
        List<List<Object>> filas = casos.stream()
                .map(c -> fila(
                        c.getNumeroCaso(),
                        c.getFechaRegistro().format(FECHA_HORA),
                        c.getCategoria().getNombre(),
                        c.getTipoIncidente().getNombre(),
                        estadoDe(c).getDescripcion(),
                        c.getNombreResidente(),
                        Optional.ofNullable(c.getNombreAnalista()).orElse("Sin asignar"),
                        c.getFechaCierre() == null ? "" : c.getFechaCierre().format(FECHA_HORA),
                        ChronoUnit.DAYS.between(c.getFechaRegistro(),
                                Objects.requireNonNullElse(c.getFechaCierre(), LocalDateTime.now()))))
                .toList();
        return new TablaExportable("Detalle de casos",
                List.of("Caso", "Registro", "Categoría", "Tipo", "Estado", "Residente", "Analista", "Cierre", "Días"),
                filas);
    }

    private TablaExportable tablaEstancados(ReporteCasosResponse r) {
        List<List<Object>> filas = r.estancados().stream()
                .map(c -> fila(c.numeroCaso(), c.tipoIncidente(), c.estadoDescripcion(),
                        Optional.ofNullable(c.nombreAnalista()).orElse("Sin asignar"),
                        c.ultimaActualizacion().format(FECHA_HORA), c.diasSinMovimiento()))
                .toList();
        return new TablaExportable("Casos estancados",
                List.of("Caso", "Tipo", "Estado", "Analista", "Último movimiento", "Días sin movimiento"), filas);
    }

    /* UTILIDADES */

    private static EstadoCaso estadoDe(Caso caso) {
        return EstadoCaso.valueOf(caso.getEstado());
    }

    private static long contar(List<Caso> casos, EstadoCaso estado) {
        return casos.stream().filter(c -> estado.name().equals(c.getEstado())).count();
    }

    private static Double horas(LocalDateTime desde, LocalDateTime hasta) {
        if (desde == null || hasta == null || hasta.isBefore(desde)) {
            return null;
        }
        return Duration.between(desde, hasta).toMinutes() / 60.0;
    }

    private static Double promedio(List<Double> valores) {
        List<Double> presentes = valores.stream().filter(Objects::nonNull).toList();
        return presentes.isEmpty()
                ? null
                : redondear(presentes.stream().mapToDouble(Double::doubleValue).average().orElse(0));
    }

    private static Double redondear(double valor) {
        return Math.round(valor * 10) / 10.0;
    }

    /* ARMA UNA FILA PERMITIENDO VALORES VACÍOS */
    private static List<Object> fila(Object... valores) {
        return Arrays.asList(valores);
    }
}
