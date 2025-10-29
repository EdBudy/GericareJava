package com.example.Gericare.Impl;

import com.example.Gericare.DTO.*;
import com.example.Gericare.Entity.*;
import com.example.Gericare.Enums.EstadoUsuario;
import com.example.Gericare.Repository.HistoriaClinicaRepository;
import com.example.Gericare.Repository.PacienteRepository;
import com.example.Gericare.Repository.UsuarioRepository;
import com.example.Gericare.Service.EnfermedadService;
import com.example.Gericare.Service.HistoriaClinicaService;
import com.example.Gericare.Service.MedicamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HistoriaClinicaServiceImpl implements HistoriaClinicaService {

    // Logger para depuración y seguimiento
    private static final Logger log = LoggerFactory.getLogger(HistoriaClinicaServiceImpl.class);

    @Autowired
    private HistoriaClinicaRepository historiaClinicaRepository;
    @Autowired
    private PacienteRepository pacienteRepository;
    @Autowired
    private UsuarioRepository usuarioRepository; // Para buscar el Administrador

    // Inyectar los servicios dedicados para catálogos
    @Autowired
    private MedicamentoService medicamentoService;
    @Autowired
    private EnfermedadService enfermedadService;

    @Override
    @Transactional
    public void crearHistoriaClinicaInicial(Paciente paciente, Administrador admin) {
        log.info("Intentando crear HC inicial para paciente ID: {}", paciente.getIdPaciente());
        // Verifica si ya existe una HC para este paciente
        if (historiaClinicaRepository.findByPacienteIdPaciente(paciente.getIdPaciente()).isEmpty()) {
            HistoriaClinica hc = new HistoriaClinica();
            hc.setPaciente(paciente);
            hc.setAdministrador(admin);
            hc.setEstado(EstadoUsuario.Activo); // Estado inicial activo
            // Campos iniciales vacíos o por defecto
            historiaClinicaRepository.save(hc);
            log.info("HC inicial creada para paciente ID: {}", paciente.getIdPaciente());
        } else {
            log.warn("Ya existe una HC para el paciente ID: {}. No se creó una nueva.", paciente.getIdPaciente());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<HistoriaClinicaDTO> obtenerHistoriaClinicaPorId(Long id) {
        log.debug("Buscando HC por ID: {}", id);
        // Usamos el metodo con FETCH JOIN para cargar todas las colecciones necesarias
        return historiaClinicaRepository.findByIdWithDetails(id).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<HistoriaClinicaDTO> obtenerHistoriaClinicaPorPacienteId(Long pacienteId) {
        log.debug("Buscando HC por Paciente ID: {}", pacienteId);
        // Usamos el metodo con FETCH JOIN para cargar todas las colecciones necesarias
        return historiaClinicaRepository.findByPacienteIdWithDetails(pacienteId).map(this::mapToDTO);
    }


    @Override
    @Transactional
    public HistoriaClinicaDTO actualizarHistoriaClinica(Long id, HistoriaClinicaDTO dto, Long adminId) { // 'id' = idHistoriaClinica (puede ser null)

        // Busca admin que realiza la operación
        Administrador admin = (Administrador) usuarioRepository.findById(adminId)
                .filter(u -> u instanceof Administrador && u.getEstado() == EstadoUsuario.Activo)
                .orElseThrow(() -> {
                    log.error("Administrador no encontrado o inactivo con id: {}", adminId);
                    return new RuntimeException("Administrador no encontrado o inactivo con id: " + adminId);
                });

        HistoriaClinica hc; // La entidad que se guardará (nueva o existente)

        if (id != null) {
            // HC existe (actualización)
            log.info("Actualizando HC ID: {} por Admin ID: {}", id, adminId);
            hc = historiaClinicaRepository.findByIdWithDetails(id)
                    .orElseThrow(() -> {
                        log.error("Historia Clínica no encontrada con id: {} para actualizar", id);
                        return new RuntimeException("Historia Clínica no encontrada con id: " + id);
                    });
            // Asegurar que el paciente no se cambie accidentalmente
            if (!hc.getPaciente().getIdPaciente().equals(dto.getIdPaciente())){
                log.error("Intento de cambiar el paciente asociado a la HC ID: {}. Original: {}, Nuevo: {}", id, hc.getPaciente().getIdPaciente(), dto.getIdPaciente());
                throw new IllegalArgumentException("No se puede cambiar el paciente de una historia clínica existente.");
            }

        } else {
            // creación
            log.info("Creando nueva HC para Paciente ID: {} por Admin ID: {}", dto.getIdPaciente(), adminId);
            Paciente paciente = pacienteRepository.findById(dto.getIdPaciente())
                    .orElseThrow(() -> {
                        log.error("Paciente ID {} no encontrado al crear HC", dto.getIdPaciente());
                        return new RuntimeException("Paciente no encontrado con ID: " + dto.getIdPaciente());
                    });

            // verificar si ya existe una HC del paciente
            Optional<HistoriaClinica> hcExistente = historiaClinicaRepository.findByPacienteIdPaciente(dto.getIdPaciente());
            if (hcExistente.isPresent()) {
                // Si si actualizar
                log.warn("Se intentó crear una HC para el paciente ID {} pero ya existe una (HC ID {}). Se procederá a actualizar la existente.", dto.getIdPaciente(), hcExistente.get().getIdHistoriaClinica());
                // Redirigir lógica a la actualización usando el ID encontrado
                return actualizarHistoriaClinica(hcExistente.get().getIdHistoriaClinica(), dto, adminId);
            }

            // Si no existe crear
            hc = new HistoriaClinica();
            hc.setPaciente(paciente);
            hc.setEstado(EstadoUsuario.Activo); // Estado inicial
        }

        // Aplicar datos del DTO a la entidad hc
        hc.setAdministrador(admin); // Quién crea o modifica
        hc.setEstadoSalud(dto.getEstadoSalud());
        hc.setCondiciones(dto.getCondiciones());
        hc.setAntecedentesMedicos(dto.getAntecedentesMedicos());
        hc.setAlergias(dto.getAlergias());
        hc.setDietasEspeciales(dto.getDietasEspeciales());
        hc.setFechaUltimaConsulta(dto.getFechaUltimaConsulta());
        hc.setObservaciones(dto.getObservaciones());

        // Actualizar/Crear Listas Relacionadas (Cirugías, Medicamentos, Enfermedades)

        // Cirugías
        hc.getCirugias().clear(); // Limpiar existentes (si hay)
        if (dto.getCirugias() != null) {
            log.debug("Procesando {} cirugías", dto.getCirugias().size());
            dto.getCirugias().forEach(cirugiaDto -> {
                if (cirugiaDto.getDescripcionCirugia() != null && !cirugiaDto.getDescripcionCirugia().isBlank()){
                    HistoriaClinicaCirugia cirugia = new HistoriaClinicaCirugia();
                    cirugia.setHistoriaClinica(hc);
                    cirugia.setDescripcionCirugia(cirugiaDto.getDescripcionCirugia());
                    cirugia.setFechaCirugia(cirugiaDto.getFechaCirugia());
                    cirugia.setObservaciones(cirugiaDto.getObservaciones());
                    cirugia.setEstado(EstadoUsuario.Activo);
                    hc.getCirugias().add(cirugia);
                }
            });
        }

        // Medicamentos
        hc.getMedicamentos().clear();
        if (dto.getMedicamentos() != null) {
            log.debug("Procesando {} medicamentos", dto.getMedicamentos().size());
            dto.getMedicamentos().forEach(medDto -> {
                // Válida que el ID del medicamento exista y esté activo
                Medicamento medEntity = medicamentoService.obtenerMedicamentoPorId(medDto.getIdMedicamento())
                        .map(mDto -> {
                            Medicamento m = new Medicamento();
                            m.setIdMedicamento(mDto.getIdMedicamento());
                            return m;
                        })
                        .orElseThrow(() -> {
                            log.error("Medicamento ID: {} no encontrado o inactivo al guardar HC", medDto.getIdMedicamento());
                            return new RuntimeException("Medicamento no encontrado o inactivo con ID: " + medDto.getIdMedicamento());
                        });

                HistoriaClinicaMedicamento hcm = new HistoriaClinicaMedicamento();
                hcm.setHistoriaClinica(hc);
                hcm.setMedicamento(medEntity);
                hcm.setDosis(medDto.getDosis());
                hcm.setFrecuencia(medDto.getFrecuencia());
                hcm.setInstrucciones(medDto.getInstrucciones());
                hcm.setEstado(EstadoUsuario.Activo);
                hc.getMedicamentos().add(hcm);
            });
        }

        // Enfermedades
        hc.getEnfermedades().clear();
        if (dto.getEnfermedades() != null) {
            log.debug("Procesando {} enfermedades", dto.getEnfermedades().size());
            dto.getEnfermedades().forEach(enfDto -> {
                // Válida que el ID de la enfermedad exista y esté activa
                Enfermedad enfEntity = enfermedadService.obtenerEnfermedadPorId(enfDto.getIdEnfermedad())
                        .map(eDto -> {
                            Enfermedad e = new Enfermedad();
                            e.setIdEnfermedad(eDto.getIdEnfermedad());
                            return e;
                        })
                        .orElseThrow(() -> {
                            log.error("Enfermedad ID: {} no encontrada o inactiva al guardar HC", enfDto.getIdEnfermedad());
                            return new RuntimeException("Enfermedad no encontrada o inactiva con ID: " + enfDto.getIdEnfermedad());
                        });

                HistoriaClinicaEnfermedad hce = new HistoriaClinicaEnfermedad();
                hce.setHistoriaClinica(hc);
                hce.setEnfermedad(enfEntity);
                hce.setFechaDiagnostico(enfDto.getFechaDiagnostico());
                hce.setObservaciones(enfDto.getObservaciones());
                hce.setEstado(EstadoUsuario.Activo);
                hc.getEnfermedades().add(hce);
            });
        }

        // Guardar HC
        try {
            HistoriaClinica hcGuardada = historiaClinicaRepository.save(hc);
            log.info("HC ID: {} {} correctamente.", hcGuardada.getIdHistoriaClinica(), (id != null ? "actualizada" : "creada"));
            // Devuelve el DTO mapeado de la entidad guardada
            return mapToDTO(hcGuardada);
        } catch (Exception e) {
            log.error("Error al guardar la HC para paciente ID: {}", dto.getIdPaciente(), e);
            throw new RuntimeException("Ocurrió un error al guardar la historia clínica.", e);
        }
    }


    // Mapeadores Helper Privados
    private HistoriaClinicaDTO mapToDTO(HistoriaClinica hc) {
        if (hc == null) return null;

        HistoriaClinicaDTO dto = new HistoriaClinicaDTO();
        dto.setIdHistoriaClinica(hc.getIdHistoriaClinica());

        // Mapeo seguro de Paciente
        if (hc.getPaciente() != null) {
            dto.setIdPaciente(hc.getPaciente().getIdPaciente());
            dto.setNombrePacienteCompleto(
                    (hc.getPaciente().getNombre() != null ? hc.getPaciente().getNombre() : "") + " " +
                            (hc.getPaciente().getApellido() != null ? hc.getPaciente().getApellido() : "")
            );
        } else {
            log.warn("HistoriaClinica ID {} no tiene Paciente asociado.", hc.getIdHistoriaClinica());
        }


        // Mapeo seguro de Administrador
        if (hc.getAdministrador() != null) {
            dto.setIdAdministrador(hc.getAdministrador().getIdUsuario());
            dto.setNombreAdministrador(
                    (hc.getAdministrador().getNombre() != null ? hc.getAdministrador().getNombre() : "") + " " +
                            (hc.getAdministrador().getApellido() != null ? hc.getAdministrador().getApellido() : "")
            );
        } else {
            log.debug("HistoriaClinica ID {} no tiene Administrador asociado (puede ser normal si no se edita).", hc.getIdHistoriaClinica());
        }


        // Mapeo de campos directos
        dto.setEstadoSalud(hc.getEstadoSalud());
        dto.setCondiciones(hc.getCondiciones());
        dto.setAntecedentesMedicos(hc.getAntecedentesMedicos());
        dto.setAlergias(hc.getAlergias());
        dto.setDietasEspeciales(hc.getDietasEspeciales());
        dto.setFechaUltimaConsulta(hc.getFechaUltimaConsulta());
        dto.setObservaciones(hc.getObservaciones());

        // Indicador de completitud (si algún campo relevante tiene contenido o hay listas no vacías)
        dto.setCompletada(
                isNotBlank(hc.getEstadoSalud()) || isNotBlank(hc.getCondiciones()) ||
                        isNotBlank(hc.getAntecedentesMedicos()) || isNotBlank(hc.getAlergias()) ||
                        isNotBlank(hc.getDietasEspeciales()) || hc.getFechaUltimaConsulta() != null || // Considerar fecha también
                        isNotBlank(hc.getObservaciones()) ||
                        (hc.getCirugias() != null && !hc.getCirugias().isEmpty()) ||
                        (hc.getMedicamentos() != null && !hc.getMedicamentos().isEmpty()) ||
                        (hc.getEnfermedades() != null && !hc.getEnfermedades().isEmpty())
        );


// Mapeo de listas (filtrando por estado activo si es necesario y manejando nulos)
        dto.setCirugias(
                Optional.ofNullable(hc.getCirugias()).orElse(Collections.emptySet()).stream()
                        .filter(c -> c.getEstado() == EstadoUsuario.Activo) // Asegurar que solo mapeamos activos
                        .map(this::mapCirugiaToDTO)
                        .collect(Collectors.toList())
        );
        dto.setMedicamentos(
                Optional.ofNullable(hc.getMedicamentos()).orElse(Collections.emptySet()).stream()
                        .filter(m -> m.getEstado() == EstadoUsuario.Activo)
                        .map(this::mapMedicamentoRelationToDTO)
                        .collect(Collectors.toList())
        );
        dto.setEnfermedades(
                Optional.ofNullable(hc.getEnfermedades()).orElse(Collections.emptySet()).stream()
                        .filter(e -> e.getEstado() == EstadoUsuario.Activo)
                        .map(this::mapEnfermedadRelationToDTO)
                        .collect(Collectors.toList())
        );

        return dto;
    }

    private HistoriaClinicaCirugiaDTO mapCirugiaToDTO(HistoriaClinicaCirugia c) {
        if (c == null) return null;
        return new HistoriaClinicaCirugiaDTO(c.getIdCirugia(), c.getDescripcionCirugia(), c.getFechaCirugia(), c.getObservaciones());
    }

    private HistoriaClinicaMedicamentoDTO mapMedicamentoRelationToDTO(HistoriaClinicaMedicamento hcm) {
        // Asegurar que las entidades anidadas no sean nulas antes de acceder a ellas
        if (hcm == null || hcm.getMedicamento() == null) {
            log.warn("Intentando mapear una relación HistoriaClinicaMedicamento nula o con Medicamento nulo.");
            return null; // O lanzar excepción si esto no debería ocurrir
        }
        return new HistoriaClinicaMedicamentoDTO(
                hcm.getIdHcMedicamento(),
                hcm.getMedicamento().getIdMedicamento(),
                hcm.getMedicamento().getNombreMedicamento(), // Requiere que Medicamento esté cargado (FETCH JOIN)
                hcm.getDosis(),
                hcm.getFrecuencia(),
                hcm.getInstrucciones()
        );
    }

    private HistoriaClinicaEnfermedadDTO mapEnfermedadRelationToDTO(HistoriaClinicaEnfermedad hce) {
        if (hce == null || hce.getEnfermedad() == null) {
            log.warn("Intentando mapear una relación HistoriaClinicaEnfermedad nula o con Enfermedad nula.");
            return null;
        }
        return new HistoriaClinicaEnfermedadDTO(
                hce.getIdHcEnfermedad(),
                hce.getEnfermedad().getIdEnfermedad(),
                hce.getEnfermedad().getNombreEnfermedad(), // Requiere que Enfermedad esté cargado (FETCH JOIN)
                hce.getFechaDiagnostico(),
                hce.getObservaciones()
        );
    }

    // Helper para verificar si un String no es null ni vacío/blanco
    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }

}