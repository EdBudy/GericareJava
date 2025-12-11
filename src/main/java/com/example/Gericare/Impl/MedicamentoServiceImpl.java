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
                // filtro pa que solo devuelva los activos
                .filter(med -> med.getEstado() == EstadoUsuario.Activo)
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
        Optional<Medicamento> existenteOpt = medicamentoRepository.findByNombreMedicamentoIgnoreCase(nombreTrimmed);

        Medicamento med;

        if (dto.getIdMedicamento() != null) {
            // Edicion
            med = medicamentoRepository.findById(dto.getIdMedicamento())
                    .orElseThrow(() -> new RuntimeException("Medicamento no encontrado con id: " + dto.getIdMedicamento()));

            // Validación de nombre duplicado en otro registro
            if (existenteOpt.isPresent() && !existenteOpt.get().getIdMedicamento().equals(dto.getIdMedicamento())) {
                // Si el otro existe pero inactivo, como esta editando uno especifico, mejor lanzar error pa evitar conflictos de IDs
                throw new RuntimeException("Ya existe otro medicamento con el nombre: " + nombreTrimmed);
            }
        } else {
            // creacion
            if (existenteOpt.isPresent()) {
                Medicamento existente = existenteOpt.get();
                // existe pero inactivo, lo reactiva
                if (existente.getEstado() == EstadoUsuario.Inactivo) {
                    med = existente; // Usa registro existente
                    // Se reactivará más abajo
                } else {
                    // Si está activo y existe, es duplicado
                    log.warn("Intento de crear medicamento duplicado (nombre: {}). Lanzando error.", nombreTrimmed);
                    throw new RuntimeException("¡El medicamento insertado ya existe!");
                }
            } else {
                // Si no existe, crea uno nuevo
                med = new Medicamento();
            }
        }

        // reactiva medicamento si esta inactivo
        med.setNombreMedicamento(nombreTrimmed);
        med.setDescripcionMedicamento(dto.getDescripcionMedicamento() != null ? dto.getDescripcionMedicamento().trim() : null);
        med.setEstado(EstadoUsuario.Activo); // Asegura que quede activo

        try {
            Medicamento medGuardado = medicamentoRepository.save(med);
            return mapToDTO(medGuardado);
        } catch (DataIntegrityViolationException e) {
            log.error("Error de integridad al guardar medicamento: {}", nombreTrimmed, e);
            throw new RuntimeException("Error al guardar el medicamento.", e);
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
    @Transactional // Si algo falla = Rollback
    public Map<String, Integer> cargarDesdeExcel(InputStream inputStream) throws Exception {
        // Map = estructura de datos que guarda info en parejas de Clave->Valor (Key-Value)
        Map<String, Integer> resultado = new HashMap<>(); // Permite devolver múltiples datos etiquetados en un solo objeto

        // Contadores para el reporte final
        int totalProcesados = 0;
        int nuevosGuardados = 0;
        int duplicadosOmitidos = 0;
        // Lista temporal (Buffer) aqui se guarda los medicamentos en memoria RAM antes de enviarlos a la BD
        List<Medicamento> medicamentosParaGuardar = new ArrayList<>();

        // Obtener todos los nombres existentes de la BD y normalizarlos (trim + lowercase)
        Set<String> nombresExistentes = medicamentoRepository.findAll().stream()
                .map(med -> med.getNombreMedicamento().trim().toLowerCase())
                .collect(Collectors.toSet());

        log.info("Iniciando carga masiva. Nombres existentes en BD: {}", nombresExistentes.size());
        // XSSFWorkbook = Apache POI
        Workbook workbook = new XSSFWorkbook(inputStream); // Abre el archivo (.xlsx)
        Sheet sheet = workbook.getSheetAt(0); // Toma la primera hoja
        Iterator<Row> rowIterator = sheet.iterator(); // Prepara el cursor para recorrer filas

        if (rowIterator.hasNext()) {
            rowIterator.next(); // Omitir fila de encabezado (Títulos: "Nombre", "Descripción")
        }

        while (rowIterator.hasNext()) { // Mientras haya filas
            Row row = rowIterator.next();
            Cell cellNombre = row.getCell(0); // Lee la Columna A (índice 0)

            // Válida que la celda no sea nula y tenga texto
            if (cellNombre != null && !cellNombre.getStringCellValue().isBlank()) {
                totalProcesados++;
                String nombreExcel = cellNombre.getStringCellValue().trim();
                String nombreNormalizado = nombreExcel.toLowerCase(); // Normaliza para comparar

                // Comprobar contra el Set de nombres existentes (Búsqueda en RAM)
                if (!nombresExistentes.contains(nombreNormalizado)) {

                    // Si no existe en el Set, crea el objeto Medicamento
                    Medicamento med = new Medicamento();
                    med.setNombreMedicamento(nombreExcel); // Guarda el nombre original

                    Cell cellDescripcion = row.getCell(1); // Columna B (Descripción)
                    if (cellDescripcion != null) {
                        med.setDescripcionMedicamento(cellDescripcion.getStringCellValue().trim());
                    }
                    med.setEstado(EstadoUsuario.Activo); // Pone el medicamento activo por defecto

                    // Lo agrega a la lista temporal (buffer)
                    medicamentosParaGuardar.add(med);

                    // Agrega el nombre al Set también por si el
                    // Excel tiene "Aspirina" en la fila 2 y otra vez en la fila 50,
                    // la segunda vez el Set verá que ya existe y no la duplica
                    nombresExistentes.add(nombreNormalizado);
                    nuevosGuardados++;
                } else {
                    // Si ya existe (en BD o en el Set), lo ignora
                    duplicadosOmitidos++;
                }
            }
        }
        workbook.close(); // Cierra el archivo para liberar memoria

        // Guarda todos los medicamentos nuevos en un solo lote
        if (!medicamentosParaGuardar.isEmpty()) {
            log.info("Guardando {} nuevos medicamentos de la carga masiva.", medicamentosParaGuardar.size());
            medicamentoRepository.saveAll(medicamentosParaGuardar);
            // En lugar de llamar a "save()" 100 veces (ósea 100 inserts) llama a "saveAll()" con una lista
            // JPA/Hibernate optimiza esto y hace una inserción masiva (Batch Insert), es más rápido y eficiente para el servidor
        }

        log.info("Carga masiva finalizada. Procesados: {}, Guardados: {}, Omitidos: {}", totalProcesados, nuevosGuardados, duplicadosOmitidos);

        resultado.put("total", totalProcesados);
        resultado.put("guardados", nuevosGuardados);
        resultado.put("omitidos", duplicadosOmitidos);
        return resultado; // Devuelve el mapa al Controlador
    }
}