package com.example.Gericare.Impl;

import com.example.Gericare.DTO.MedicamentoDTO;
import com.example.Gericare.Entity.Medicamento;
import com.example.Gericare.Enums.EstadoUsuario;
import com.example.Gericare.Repository.MedicamentoRepository;
import com.example.Gericare.Service.MedicamentoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import com.example.Gericare.specification.MedicamentoSpecification;
import org.springframework.dao.DataIntegrityViolationException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.*;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MedicamentoServiceImpl implements MedicamentoService {

    private static final Logger log = LoggerFactory.getLogger(MedicamentoServiceImpl.class);

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MedicamentoDTO> listarMedicamentosActivos() {
        return medicamentoRepository.findAll().stream()
                .filter(med -> med.getEstado() == EstadoUsuario.Activo)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicamentoDTO> listarMedicamentosActivosFiltrados(String nombre, String descripcion) {
        Specification<Medicamento> spec = MedicamentoSpecification.findByCriteria(nombre, descripcion);
        return medicamentoRepository.findAll(spec).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MedicamentoDTO guardarMedicamento(MedicamentoDTO dto) {
        if (dto.getNombreMedicamento() == null || dto.getNombreMedicamento().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del medicamento no puede estar vacío.");
        }

        String nombreTrimmed = dto.getNombreMedicamento().trim();

        // Buscar si ya existe un medicamento con ese nombre (ignorando mayúsculas/minúsculas)
        Optional<Medicamento> existenteOpt = medicamentoRepository.findByNombreMedicamentoIgnoreCase(nombreTrimmed);

        Medicamento med;

        if (dto.getIdMedicamento() != null) {
            med = medicamentoRepository.findById(dto.getIdMedicamento())
                    .orElseThrow(() -> new RuntimeException("Medicamento no encontrado con id: " + dto.getIdMedicamento()));

            // Si está intentando cambiar el nombre a uno que ya existe en otro registro
            if (existenteOpt.isPresent() && !existenteOpt.get().getIdMedicamento().equals(dto.getIdMedicamento())) {
                // Lanza error, porque es una acción manual de edición
                log.warn("Intento de renombrar medicamento ID {} al nombre de ID {}", dto.getIdMedicamento(), existenteOpt.get().getIdMedicamento());
                throw new RuntimeException("Ya existe otro medicamento con el nombre: " + nombreTrimmed);
            }
        } else {
            // Creación
            // Si ya existe, LANZA EL ERROR
            if (existenteOpt.isPresent()) {
                log.warn("Intento de crear medicamento duplicado (nombre: {}). Lanzando error.", nombreTrimmed);
                // Este es el error que tu controlador capturará
                throw new RuntimeException("¡El medicamento insertado ya existe!");
            }
            // Si no existe crea uno nuevo
            med = new Medicamento();
        }

        // Aplicar cambios
        med.setNombreMedicamento(nombreTrimmed); // Guardar con el formato enviado (solo con trim)
        med.setDescripcionMedicamento(dto.getDescripcionMedicamento() != null ? dto.getDescripcionMedicamento().trim() : null);
        med.setEstado(EstadoUsuario.Activo);

        try {
            Medicamento medGuardado = medicamentoRepository.save(med);
            return mapToDTO(medGuardado);
        } catch (DataIntegrityViolationException e) {
            log.error("Error de integridad al guardar medicamento: {}", nombreTrimmed, e);
            throw new RuntimeException("Ya existe un medicamento con el nombre: " + nombreTrimmed, e);
        }
    }

    @Override
    @Transactional
    public void eliminarMedicamento(Long id) {
        medicamentoRepository.findById(id).ifPresent(med -> {
            if (med.getEstado() == EstadoUsuario.Activo) { // Solo inactivar si está activo
                med.setEstado(EstadoUsuario.Inactivo);
                medicamentoRepository.save(med);
            }
        });
        // Si no se encuentra, simplemente no hacer nada (o lanzar excepción si se prefiere)
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MedicamentoDTO> obtenerMedicamentoPorId(Long id) {
        return medicamentoRepository.findById(id)
                // Solo devolver si está activo
                .filter(med -> med.getEstado() == EstadoUsuario.Activo)
                .map(this::mapToDTO);
    }

    // Mapeador privado
    private MedicamentoDTO mapToDTO(Medicamento med) {
        if (med == null) return null;
        return new MedicamentoDTO(med.getIdMedicamento(), med.getNombreMedicamento(), med.getDescripcionMedicamento());
    }

    //Carga masiva de datos (medicamentos)
    @Override
    @Transactional
    public Map<String, Integer> cargarDesdeExcel(InputStream inputStream) throws Exception {
        Map<String, Integer> resultado = new HashMap<>();
        int totalProcesados = 0;
        int nuevosGuardados = 0;
        int duplicadosOmitidos = 0;

        List<Medicamento> medicamentosParaGuardar = new ArrayList<>();

        // Obtener todos los nombres existentes de la BD y normalizarlos (trim + lowercase)
        Set<String> nombresExistentes = medicamentoRepository.findAll().stream()
                .map(med -> med.getNombreMedicamento().trim().toLowerCase())
                .collect(Collectors.toSet());

        log.info("Iniciando carga masiva. Nombres existentes en BD: {}", nombresExistentes.size());

        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();

        if (rowIterator.hasNext()) {
            rowIterator.next(); // Omitir fila de encabezado
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Cell cellNombre = row.getCell(0); // Columna A (Nombre)

            if (cellNombre != null && !cellNombre.getStringCellValue().isBlank()) {
                totalProcesados++;
                String nombreExcel = cellNombre.getStringCellValue().trim();
                String nombreNormalizado = nombreExcel.toLowerCase();

                // Comprobar contra el Set de nombres existentes
                if (!nombresExistentes.contains(nombreNormalizado)) {
                    // Si no existe lo prepara para guardar
                    Medicamento med = new Medicamento();
                    med.setNombreMedicamento(nombreExcel); // Guarda el nombre original (solo con trim)

                    Cell cellDescripcion = row.getCell(1); // Columna B (Descripción)
                    if (cellDescripcion != null) {
                        med.setDescripcionMedicamento(cellDescripcion.getStringCellValue().trim());
                    }
                    med.setEstado(EstadoUsuario.Activo);

                    medicamentosParaGuardar.add(med);

                    // Añadir el nuevo nombre al Set para evitar duplicados (dentro del mismo Excel)
                    nombresExistentes.add(nombreNormalizado);
                    nuevosGuardados++;
                } else {
                    // Si ya existe (en BD o en el Set) lo omite
                    duplicadosOmitidos++;
                }
            }
        }

        workbook.close();

        // Guarda todos los medicamentos nuevos en un solo lote
        if (!medicamentosParaGuardar.isEmpty()) {
            log.info("Guardando {} nuevos medicamentos de la carga masiva.", medicamentosParaGuardar.size());
            medicamentoRepository.saveAll(medicamentosParaGuardar);
        }

        log.info("Carga masiva finalizada. Procesados: {}, Guardados: {}, Omitidos: {}", totalProcesados, nuevosGuardados, duplicadosOmitidos);

        resultado.put("total", totalProcesados);
        resultado.put("guardados", nuevosGuardados);
        resultado.put("omitidos", duplicadosOmitidos);
        return resultado;
    }
}