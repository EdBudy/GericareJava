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

    // logger para registrar eventos en el sistema
    private static final Logger log = LoggerFactory.getLogger(MedicamentoServiceImpl.class);

    // inyecta el repositorio para acceder a la base de datos
    @Autowired
    private MedicamentoRepository medicamentoRepository;

    // metodo para listar todos los medicamentos activos
    @Override
    @Transactional(readOnly = true)
    public List<MedicamentoDTO> listarMedicamentosActivos() {
        return medicamentoRepository.findAll().stream()
                // filtra solo los medicamentos con estado activo
                .filter(med -> med.getEstado() == EstadoUsuario.Activo)
                // convierte cada entidad a un dto
                .map(this::mapToDTO)
                // recoge los resultados en una lista
                .collect(Collectors.toList());
    }

    // metodo para listar medicamentos activos con filtros
    @Override
    @Transactional(readOnly = true)
    public List<MedicamentoDTO> listarMedicamentosActivosFiltrados(String nombre, String descripcion) {
        // crea una especificacion con los criterios de busqueda
        Specification<Medicamento> spec = MedicamentoSpecification.findByCriteria(nombre, descripcion);
        return medicamentoRepository.findAll(spec).stream()
                // filtra solo los medicamentos activos
                .filter(med -> med.getEstado() == EstadoUsuario.Activo)
                // convierte cada entidad a dto
                .map(this::mapToDTO)
                // recoge los resultados en una lista
                .collect(Collectors.toList());
    }

    // metodo para guardar o actualizar un medicamento
    @Override
    @Transactional
    public MedicamentoDTO guardarMedicamento(MedicamentoDTO dto) {
        // valida que el nombre no este vacio
        if (dto.getNombreMedicamento() == null || dto.getNombreMedicamento().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del medicamento no puede estar vacío.");
        }

        // quita espacios en blanco al inicio y final del nombre
        String nombreTrimmed = dto.getNombreMedicamento().trim();
        // busca si ya existe un medicamento con ese nombre
        Optional<Medicamento> existenteOpt = medicamentoRepository.findByNombreMedicamentoIgnoreCase(nombreTrimmed);

        // declara la variable para el medicamento
        Medicamento med;

        // si el dto tiene id, significa que es una edicion
        if (dto.getIdMedicamento() != null) {
            // busca el medicamento por id
            med = medicamentoRepository.findById(dto.getIdMedicamento())
                    .orElseThrow(() -> new RuntimeException("Medicamento no encontrado con id: " + dto.getIdMedicamento()));

            // valida que no exista otro medicamento con el mismo nombre
            if (existenteOpt.isPresent() && !existenteOpt.get().getIdMedicamento().equals(dto.getIdMedicamento())) {
                // Si el otro existe pero inactivo, como esta editando uno especifico, mejor lanzar error pa evitar conflictos de IDs
                throw new RuntimeException("Ya existe otro medicamento con el nombre: " + nombreTrimmed);
            }
        } else {
            // si no tiene id, es una creacion
            if (existenteOpt.isPresent()) {
                Medicamento existente = existenteOpt.get();
                // si el medicamento existe pero esta inactivo, lo reactiva
                if (existente.getEstado() == EstadoUsuario.Inactivo) {
                    med = existente; // usa el registro existente
                } else {
                    // Si está activo y existe, es duplicado
                    log.warn("Intento de crear medicamento duplicado (nombre: {}). Lanzando error.", nombreTrimmed);
                    throw new RuntimeException("¡El medicamento insertado ya existe!");
                }
            } else {
                // si no existe, crea uno nuevo
                med = new Medicamento();
            }
        }

        // asigna los valores del dto a la entidad
        med.setNombreMedicamento(nombreTrimmed);
        med.setDescripcionMedicamento(dto.getDescripcionMedicamento() != null ? dto.getDescripcionMedicamento().trim() : null);
        med.setEstado(EstadoUsuario.Activo); // asegura que quede activo

        try {
            // guarda el medicamento en la base de datos
            Medicamento medGuardado = medicamentoRepository.save(med);
            // convierte la entidad guardada a dto y la retorna
            return mapToDTO(medGuardado);
        } catch (DataIntegrityViolationException e) {
            log.error("Error de integridad al guardar medicamento: {}", nombreTrimmed, e);
            throw new RuntimeException("Error al guardar el medicamento.", e);
        }
    }

    // metodo para eliminar (inactivar) un medicamento
    @Override
    @Transactional
    public void eliminarMedicamento(Long id) {
        // busca el medicamento por id
        medicamentoRepository.findById(id).ifPresent(med -> {
            // si esta activo, lo inactiva
            if (med.getEstado() == EstadoUsuario.Activo) {
                med.setEstado(EstadoUsuario.Inactivo);
                medicamentoRepository.save(med);
            }
        });
        // si no se encuentra, no hace nada
    }

    // metodo para obtener un medicamento por su id
    @Override
    @Transactional(readOnly = true)
    public Optional<MedicamentoDTO> obtenerMedicamentoPorId(Long id) {
        return medicamentoRepository.findById(id)
                // filtra solo si esta activo
                .filter(med -> med.getEstado() == EstadoUsuario.Activo)
                // convierte a dto
                .map(this::mapToDTO);
    }

    // metodo privado para convertir entidad a dto
    private MedicamentoDTO mapToDTO(Medicamento med) {
        if (med == null) return null;
        return new MedicamentoDTO(med.getIdMedicamento(), med.getNombreMedicamento(), med.getDescripcionMedicamento());
    }

    // metodo para cargar medicamentos desde un archivo excel
    @Override
    @Transactional // Si algo falla = Rollback
    public Map<String, Integer> cargarDesdeExcel(InputStream inputStream) throws Exception {
        // Map = estructura de datos que guarda info en parejas de Clave->Valor (Key-Value)
        Map<String, Integer> resultado = new HashMap<>(); // Permite devolver múltiples datos etiquetados en un solo objeto

        // contador para el total de filas procesadas
        int totalProcesados = 0;
        // contador para los nuevos medicamentos guardados
        int nuevosGuardados = 0;
        // contador para los duplicados omitidos
        int duplicadosOmitidos = 0;
        // lista temporal para almacenar los medicamentos antes de guardarlos
        List<Medicamento> medicamentosParaGuardar = new ArrayList<>();

        // obtiene todos los nombres de medicamentos existentes en la base de datos
        Set<String> nombresExistentes = medicamentoRepository.findAll().stream()
                // normaliza los nombres (minusculas y sin espacios)
                .map(med -> med.getNombreMedicamento().trim().toLowerCase())
                // los recoge en un conjunto para busqueda rapida
                .collect(Collectors.toSet());

        log.info("Iniciando carga masiva. Nombres existentes en BD: {}", nombresExistentes.size());
        // XSSFWorkbook = Apache POI
        Workbook workbook = new XSSFWorkbook(inputStream); // Abre el archivo (.xlsx)
        Sheet sheet = workbook.getSheetAt(0); // Toma la primera hoja
        Iterator<Row> rowIterator = sheet.iterator(); // Prepara el cursor para recorrer filas

        // si hay filas, omite la primera (encabezados)
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) { // Mientras haya filas
            Row row = rowIterator.next();
            Cell cellNombre = row.getCell(0); // Lee la Columna A (índice 0)

            // valida que la celda no sea nula y no este vacia
            if (cellNombre != null && !cellNombre.getStringCellValue().isBlank()) {
                // incrementa el contador de procesados
                totalProcesados++;
                // obtiene el nombre de la celda y le quita espacios
                String nombreExcel = cellNombre.getStringCellValue().trim();
                // normaliza el nombre a minusculas para comparar
                String nombreNormalizado = nombreExcel.toLowerCase();

                // verifica si el nombre ya existe en el conjunto
                if (!nombresExistentes.contains(nombreNormalizado)) {

                    // Si no existe en el Set, crea el nuevo objeto Medicamento
                    Medicamento med = new Medicamento();
                    // asigna el nombre original
                    med.setNombreMedicamento(nombreExcel);

                    // obtiene la celda de la columna b (descripcion)
                    Cell cellDescripcion = row.getCell(1);
                    // si tiene descripcion, la asigna
                    if (cellDescripcion != null) {
                        med.setDescripcionMedicamento(cellDescripcion.getStringCellValue().trim());
                    }
                    // establece el estado como activo
                    med.setEstado(EstadoUsuario.Activo);

                    // agrega el medicamento a la lista temporal
                    medicamentosParaGuardar.add(med);

                    // Agrega el nombre al Set también por si el
                    // Excel tiene "Aspirina" en la fila 2 y otra vez en la fila 50,
                    // la segunda vez el Set verá que ya existe y no la duplica
                    nombresExistentes.add(nombreNormalizado);
                    // incrementa el contador de guardados
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

        // agrega los resultados al mapa
        resultado.put("total", totalProcesados);
        resultado.put("guardados", nuevosGuardados);
        resultado.put("omitidos", duplicadosOmitidos);
        // retorna el mapa con los resultados
        return resultado;
    }
}