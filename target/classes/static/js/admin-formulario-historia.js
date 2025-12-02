// Variable global interna del script
let medicamentoSelectTarget = null;

// Las variables 'medicamentosCatalogo', 'csrfToken', y 'csrfHeaderName' ya vienen definidas desde el archivo HTML (formulario-historia.html).

// Genera las opciones HTML para un <select>
function generateOptions(catalogo, valueField, textField, selectedValue = null) {
    if (!catalogo || catalogo.length === 0) {
        return '<option value="" disabled>-- No hay opciones --</option>';
    }
    return catalogo.map(item =>
        `<option value="${item[valueField]}" ${selectedValue == item[valueField] ? 'selected' : ''}>${item[textField]}</option>`
    ).join('');
}

// Elimina un item dinámico (cirugía, medicamento, enfermedad)
function removeItem(button) {
    const item = button.closest('.dynamic-item-row');
    if (item) {
        const wrapperId = item.parentElement.id;
        item.remove();
        // Reindexar después de eliminar
        if (wrapperId === 'cirugias-wrapper') reindexItems(wrapperId, 'cirugias');
        if (wrapperId === 'medicamentos-wrapper') reindexItems(wrapperId, 'medicamentos');
        if (wrapperId === 'enfermedades-wrapper') reindexItems(wrapperId, 'enfermedades');
    }
}

// Reajusta los índices [i] en los nombres de los inputs
function reindexItems(wrapperId, baseName) {
    const wrapper = document.getElementById(wrapperId);
    if (!wrapper) return;
    const items = wrapper.children;
    for (let i = 0; i < items.length; i++) {
        const inputs = items[i].querySelectorAll('input, select, textarea');
        inputs.forEach(input => {
            const name = input.getAttribute('name');
            if (name && name.startsWith(baseName + '[')) {
                const newName = name.replace(/\[\d+\]/, `[${i}]`);
                input.setAttribute('name', newName);
                if(input.id && input.id.includes('-')) {
                    const baseId = input.id.substring(0, input.id.lastIndexOf('-'));
                    input.id = `${baseId}-${i}`;
                    const label = items[i].querySelector(`label[for="${input.id.replace(`-${i}`, '-\\d+') }"]`);
                    if(label) label.setAttribute('for', input.id);
                }
            }
        });
    }
}

// Funciones para Cirugías
function addCirugiaItem() {
    const wrapper = document.getElementById('cirugias-wrapper');
    const index = wrapper.children.length;
    const newItem = document.createElement('div');
    newItem.className = 'row dynamic-item-row cirugia-item align-items-end';
    newItem.innerHTML = `
        <input type="hidden" name="cirugias[${index}].idCirugia" value="" />
        <div class="col-md-5">
            <label for="cirugia-desc-${index}" class="form-label small">Descripción:</label>
            <input type="text" id="cirugia-desc-${index}" class="form-control form-control-sm" name="cirugias[${index}].descripcionCirugia" required />
        </div>
        <div class="col-md-3">
            <label for="cirugia-fecha-${index}" class="form-label small">Fecha:</label>
            <input type="date" id="cirugia-fecha-${index}" class="form-control form-control-sm" name="cirugias[${index}].fechaCirugia" />
        </div>
        <div class="col-md-3">
            <label for="cirugia-obs-${index}" class="form-label small">Observaciones:</label>
            <input type="text" id="cirugia-obs-${index}" class="form-control form-control-sm" name="cirugias[${index}].observaciones" />
        </div>
        <div class="col-md-1">
            <button type="button" class="btn btn-danger btn-sm w-100" onclick="removeItem(this)" title="Eliminar Cirugía">
                <i class="bi bi-trash"></i>
            </button>
        </div>
    `;
    wrapper.appendChild(newItem);
}

// Funciones para Medicamentos
function addMedicamentoItem() {
    // Calcula índice contando cuantos elementos con clase .medicamento-item hay
    var index = document.querySelectorAll('.medicamento-item').length;

    var wrapper = document.getElementById('medicamentos-wrapper');

    // Crea div contenedor de la fila
    var newRow = document.createElement('div');
    newRow.className = 'row dynamic-item-row medicamento-item align-items-end mb-3';

    // Construir HTML (igual al que esta en el Thymeleaf)
    newRow.innerHTML = `
        <div class="col-md-3">
            <label class="form-label">Medicamento:</label>
            <select class="form-select form-select-sm" name="medicamentos[${index}].idMedicamento" required>
                 <option value="" disabled selected>Seleccione...</option>
                 ${generarOpcionesMedicamentos()}
            </select>
            <button type="button" class="btn btn-link btn-sm p-0 mt-1" onclick="abrirModalNuevoMedicamento(this)">+ Nuevo Medicamento</button>
        </div>

        <div class="col-md-2">
            <label class="form-label">Dosis (en mg):</label>
            <div class="input-group input-group-sm">
                <input type="number" step="any" min="0"
                       class="form-control"
                       name="medicamentos[${index}].dosis"
                       placeholder="0" required />
                <span class="input-group-text">mg</span>
            </div>
        </div>

        <div class="col-md-2">
            <label class="form-label">Frecuencia:</label>
            <input type="text" class="form-control form-control-sm" name="medicamentos[${index}].frecuencia" placeholder="Ej: Cada 8h"/>
        </div>

        <div class="col-md-3">
            <label class="form-label">Instrucciones:</label>
            <input type="text" class="form-control form-control-sm" name="medicamentos[${index}].instrucciones" />
        </div>

        <div class="col-md-1">
             <button type="button" class="btn btn-danger btn-sm w-100" onclick="removeItem(this)" title="Eliminar Medicamento">
                <i class="bi bi-trash"></i>
            </button>
        </div>
        <div class="col-md-1"></div>
    `;

    wrapper.appendChild(newRow);
}

// Función auxiliar para repoblar el select
function generarOpcionesMedicamentos() {
    // 'medicamentosCatalogo' es la variable global que define en el HTML
    if (typeof medicamentosCatalogo === 'undefined' || !medicamentosCatalogo) {
        return '';
    }
    return medicamentosCatalogo.map(m =>
        `<option value="${m.idMedicamento}">${m.nombreMedicamento}</option>`
    ).join('');
}

function abrirModalNuevoMedicamento(button) {
    medicamentoSelectTarget = button.closest('.medicamento-item').querySelector('select');
    document.getElementById('formNuevoMedicamento').reset();
    document.getElementById('nuevoMedNombre').classList.remove('is-invalid');
    document.getElementById('errorNuevoMedicamento').textContent = '';
    const modalElement = document.getElementById('modalNuevoMedicamento');
    const modal = bootstrap.Modal.getOrCreateInstance(modalElement);
    modal.show();
}

function guardarNuevoMedicamento() {
    const nombreInput = document.getElementById('nuevoMedNombre');
    const nombre = nombreInput.value.trim();
    const descripcion = document.getElementById('nuevoMedDesc').value.trim();
    const errorDiv = document.getElementById('errorNuevoMedicamento');
    const submitButton = document.querySelector('#modalNuevoMedicamento .modal-footer button.btn-primary');
    errorDiv.textContent = '';
    nombreInput.classList.remove('is-invalid');

    if (!nombre) {
        errorDiv.textContent = 'El nombre es obligatorio.';
        nombreInput.classList.add('is-invalid');
        return;
    }

    submitButton.disabled = true;
    submitButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Guardando...';

    const data = { nombreMedicamento: nombre, descripcionMedicamento: descripcion };
    const headers = { 'Content-Type': 'application/json' };

    // Usar las variables globales de CSRF
    const csrfTokenModal = document.getElementById('csrfTokenModalMed')?.value;
    if (csrfHeaderName && csrfTokenModal) {
        headers[csrfHeaderName] = csrfTokenModal;
    } else if (csrfHeaderName && csrfToken) {
        headers[csrfHeaderName] = csrfToken;
    }

    fetch('/medicamentos/nuevo-ajax', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify(data)
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => {
                throw new Error(text || `Error ${response.status}: ${response.statusText}`);
            });
        }
        return response.json();
    })
    .then(nuevoMedicamento => {
        // Actualizar el catálogo global
        medicamentosCatalogo.push(nuevoMedicamento);

        const nuevaOpcionHTML = `<option value="${nuevoMedicamento.idMedicamento}">${nuevoMedicamento.nombreMedicamento}</option>`;

        // Añadir la nueva opción a TODOS los selects de medicamento
        document.querySelectorAll('#medicamentos-wrapper select[name*="idMedicamento"]').forEach(select => {
            if (!select.querySelector(`option[value="${nuevoMedicamento.idMedicamento}"]`)) {
                select.insertAdjacentHTML('beforeend', nuevaOpcionHTML);
            }
        });

        // Seleccionar la nueva opción
        if (medicamentoSelectTarget) {
            medicamentoSelectTarget.value = nuevoMedicamento.idMedicamento;
        }

        const modalElement = document.getElementById('modalNuevoMedicamento');
        const modal = bootstrap.Modal.getInstance(modalElement);
        if (modal) {
            modal.hide();
        }
        medicamentoSelectTarget = null;
    })
    .catch(error => {
        console.error('Error al guardar medicamento vía AJAX:', error);
        errorDiv.textContent = `Error: ${error.message}`;
    })
    .finally(() => {
        submitButton.disabled = false;
        submitButton.innerHTML = 'Guardar y Seleccionar';
    });
}

// Funciones para Enfermedades
function addEnfermedadItem() {
    const wrapper = document.getElementById('enfermedades-wrapper');
    const index = wrapper.getElementsByClassName('enfermedad-item').length;
    const newItem = document.createElement('div');
    newItem.className = 'row dynamic-item-row enfermedad-item align-items-end';
    newItem.innerHTML = `
        <input type="hidden" name="enfermedades[${index}].idHcEnfermedad" value="" />
        <div class="col-md-5">
            <label for="enf-desc-${index}" class="form-label small">Enfermedad/Condición:</label>
            <input type="text" id="enf-desc-${index}" class="form-control form-control-sm" name="enfermedades[${index}].descripcionEnfermedad" required />
        </div>
        <div class="col-md-3">
            <label for="enf-fecha-${index}" class="form-label small">Fecha Diagnóstico:</label>
            <input type="date" id="enf-fecha-${index}" class="form-control form-control-sm" name="enfermedades[${index}].fechaDiagnostico" />
        </div>
        <div class="col-md-3">
            <label for="enf-obs-${index}" class="form-label small">Observaciones:</label>
            <input type="text" id="enf-obs-${index}" class="form-control form-control-sm" name="enfermedades[${index}].observaciones" />
        </div>
        <div class="col-md-1">
            <button type="button" class="btn btn-danger btn-sm w-100" onclick="removeItem(this)" title="Eliminar Enfermedad">
                <i class="bi bi-trash"></i>
            </button>
        </div>
    `;
    wrapper.appendChild(newItem);
}


// Inicialización al Cargar la Página
document.addEventListener('DOMContentLoaded', function() {

    // Poblar selects de medicamentos existentes
    document.querySelectorAll('#medicamentos-wrapper select[name*="idMedicamento"]').forEach(select => {
        const selectedValue = select.getAttribute('data-selected-id');
        select.innerHTML = '<option value="">Seleccione...</option>' + generateOptions(medicamentosCatalogo, 'idMedicamento', 'nombreMedicamento', selectedValue);
    });

    // Reindexar items al cargar
    reindexItems('cirugias-wrapper', 'cirugias');
    reindexItems('medicamentos-wrapper', 'medicamentos');
    reindexItems('enfermedades-wrapper', 'enfermedades');
});