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
    @Transactional
    public Map<String, Integer> cargarDesdeExcel(InputStream inputStream) throws Exception {
        // crea un mapa para retornar los resultados del proceso
        Map<String, Integer> resultado = new HashMap<>();

        // inicializa contadores para el reporte
        int totalProcesados = 0;
        int nuevosGuardados = 0;
        int duplicadosOmitidos = 0;

        // crea una lista para agrupar los medicamentos que se guardaran
        List<Medicamento> medicamentosParaGuardar = new ArrayList<>();

        // carga todos los medicamentos de la base de datos en un mapa para acceso rapido
        // usa el nombre normalizado como clave y el objeto entidad como valor
        Map<String, Medicamento> mapaExistentes = medicamentoRepository.findAll().stream()
                .collect(Collectors.toMap(
                        m -> m.getNombreMedicamento().trim().toLowerCase(),
                        m -> m,
                        (m1, m2) -> m1
                ));

        log.info("Iniciando carga masiva. Registros en BD: {}", mapaExistentes.size());

        // abre el archivo excel usando apache poi
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();

        // salta la primera fila si contiene encabezados
        if (rowIterator.hasNext()) rowIterator.next();

        // recorre cada fila del archivo excel
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Cell cellNombre = row.getCell(0);

            // procesa la fila solo si la celda del nombre tiene contenido
            if (cellNombre != null && !cellNombre.getStringCellValue().isBlank()) {
                totalProcesados++;
                String nombreExcel = cellNombre.getStringCellValue().trim();
                String nombreNormalizado = nombreExcel.toLowerCase();

                // verifica si el medicamento ya existe en el mapa de memoria
                if (mapaExistentes.containsKey(nombreNormalizado)) {
                    // recupera la entidad existente del mapa
                    Medicamento existente = mapaExistentes.get(nombreNormalizado);

                    // revisa si el medicamento estaba inactivo
                    if (existente.getEstado() == EstadoUsuario.Inactivo) {
                        // reactiva el medicamento cambiando su estado a activo
                        existente.setEstado(EstadoUsuario.Activo);

                        // actualiza la descripcion si existe en el excel
                        Cell cellDesc = row.getCell(1);
                        if (cellDesc != null) {
                            existente.setDescripcionMedicamento(cellDesc.getStringCellValue().trim());
                        }

                        // agrega el medicamento reactivado a la lista de guardado
                        medicamentosParaGuardar.add(existente);
                        nuevosGuardados++;
                    } else {
                        // cuenta como duplicado si ya existe y esta activo
                        duplicadosOmitidos++;
                    }
                } else {
                    // crea una nueva entidad si no existe en el mapa
                    Medicamento med = new Medicamento();
                    med.setNombreMedicamento(nombreExcel);

                    Cell cellDescripcion = row.getCell(1);
                    if (cellDescripcion != null) {
                        med.setDescripcionMedicamento(cellDescripcion.getStringCellValue().trim());
                    }
                    med.setEstado(EstadoUsuario.Activo);

                    // agrega el nuevo medicamento a la lista
                    medicamentosParaGuardar.add(med);

                    // actualiza el mapa temporal para evitar duplicados dentro del mismo archivo
                    mapaExistentes.put(nombreNormalizado, med);
                    nuevosGuardados++;
                }
            }
        }
        // cierra el recurso del workbook para liberar memoria
        workbook.close();

        // guarda o actualiza todos los registros acumulados en un solo lote
        if (!medicamentosParaGuardar.isEmpty()) {
            log.info("Guardando/Reactivando {} medicamentos.", medicamentosParaGuardar.size());
            medicamentoRepository.saveAll(medicamentosParaGuardar);
        }

        log.info("Carga finalizada. Total: {}, Guardados: {}, Omitidos: {}", totalProcesados, nuevosGuardados, duplicadosOmitidos);

        // prepara el mapa de resultados final
        resultado.put("total", totalProcesados);
        resultado.put("guardados", nuevosGuardados);
        resultado.put("omitidos", duplicadosOmitidos);
        return resultado;
    }
}