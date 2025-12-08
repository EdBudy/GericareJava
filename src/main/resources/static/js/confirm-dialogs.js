function confirmarEliminacion(button) {
    const usuarioId = button.getAttribute('data-usuario-id');
    const rol = button.getAttribute('data-usuario-rol');
    const nombreUsuario = button.getAttribute('data-usuario-nombre');
    const warningText = button.getAttribute('data-warning-text'); // Texto para familiar asignado

    let title = '¿Estás seguro?';
    let text = `¡El usuario ${nombreUsuario} será eliminado (lógicamente) y no podrás revertir esto!`;
    let icon = 'warning';
    let confirmButtonText = 'Sí, ¡Eliminar!';
    let confirmButtonColor = '#d33';

    // Si existe un texto de advertencia (para familiares con pacientes) se usa
    if (warningText) {
        title = 'Advertencia de Asignación';
        text = warningText; // Usa el texto dinámico que viene del backend
    }

    Swal.fire({
        title: title,
        text: text,
        icon: icon,
        showCancelButton: true,
        confirmButtonColor: confirmButtonColor,
        cancelButtonColor: '#3085d6',
        confirmButtonText: confirmButtonText,
        cancelButtonText: 'Cancelar'
    }).then((result) => {
        if (result.isConfirmed) {
            const form = document.getElementById('form-eliminar-' + usuarioId);
            if (form) {
                form.submit();
            } else {
                console.error(`Formulario form-eliminar-${usuarioId} no encontrado`);
            }
        }
    });
}

// Paciente (Gestión Paciente Admin)
function confirmarEliminacionPaciente(pacienteId) {
    Swal.fire({
        title: '¿Estás seguro?',
        text: "¡El paciente y todos sus datos asociados (asignaciones, actividades, solicitudes, tratamientos) serán eliminados!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Sí, ¡Eliminar!',
        cancelButtonText: 'Cancelar'
    }).then((result) => {
        if (result.isConfirmed) {
            const form = document.getElementById('form-eliminar-paciente-' + pacienteId);
             if (form) {
                form.submit();
            } else {
                console.error(`Formulario form-eliminar-paciente-${pacienteId} no encontrado`);
            }
        }
    });
}

// Actividad (Actividades Admin)
function confirmarEliminacionActividad(actividadId) {
    Swal.fire({
        title: '¿Estás seguro?',
        text: "¡La actividad se eliminara!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Sí, ¡Eliminar!',
        cancelButtonText: 'Cancelar'
    }).then((result) => {
        if (result.isConfirmed) {
            const form = document.getElementById('form-eliminar-actividad-' + actividadId);
             if (form) {
                form.submit();
            } else {
                console.error(`Formulario form-eliminar-actividad-${actividadId} no encontrado`);
            }
        }
    });
}

// Solicitud (Familiar solicitudes)
function confirmarEliminacionSolicitudFamiliar(solicitudId) {
    Swal.fire({
        title: '¿Cancelar Solicitud?',
        text: "Tu solicitud pendiente será eliminada.",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Sí, ¡Cancelar Solicitud!',
        cancelButtonText: 'No cancelar'
    }).then((result) => {
        if (result.isConfirmed) {
             const form = document.querySelector(`form[action='/solicitudes/eliminar/${solicitudId}']`);
             if(form) {
                form.submit();
             } else {
                 console.error("Formulario de eliminación no encontrado para solicitud ID:", solicitudId);
             }
        }
    });
}

// Solicitud (Admin solicitudes)
function confirmarArchivarSolicitudAdmin(solicitudId) {
    Swal.fire({
        title: '¿Eliminar Solicitud?',
        text: "La solicitud se eliminara. ¿Deseas continuar?",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#3085d6',
        cancelButtonColor: '#6c757d',
        confirmButtonText: 'Sí, ¡Archivar!',
        cancelButtonText: 'Cancelar'
    }).then((result) => {
        if (result.isConfirmed) {
             const form = document.querySelector(`form[action='/solicitudes/eliminar/${solicitudId}']`);
             if(form) {
                form.submit();
             } else {
                 console.error("Formulario de archivar no encontrado para solicitud ID:", solicitudId);
             }
        }
    });
}

// Tratamiento (Admin tratamientos)
function confirmarEliminacionTratamiento(id) {
     Swal.fire({
        title: '¿Esta seguro?',
        text: "¡El tratamiento será eliminado y no podra revertir esto!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Sí, ¡Eliminar!',
        cancelButtonText: 'Cancelar'
    }).then((result) => {
        if (result.isConfirmed) {
             const form = document.getElementById(`form-eliminar-tratamiento-${id}`);
             if(form) form.submit();
             else console.error(`Formulario form-eliminar-tratamiento-${id} no encontrado`);
        }
    });
}


// Confirmaciones de Cambio de Estado

// Tratamiento (Cuidador tratamientos)
function confirmarCompletarTratamiento(id) {
     Swal.fire({
        title: '¿Marcar como Completado?',
        text: "Confirmas que el tratamiento ha sido completado.",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#28a745',
        cancelButtonColor: '#6c757d',
        confirmButtonText: 'Sí, ¡Completado!',
        cancelButtonText: 'Cancelar'
    }).then((result) => {
        if (result.isConfirmed) {
             const form = document.getElementById(`form-completar-tratamiento-${id}`);
             if(form) form.submit();
             else console.error(`Formulario form-completar-tratamiento-${id} no encontrado`);
        }
    });
}

// Completar tratamiento cuidador
function confirmarCompletar(tratamientoId) {
    Swal.fire({
        title: '¿Completar Tratamiento?',
        text: "El tratamiento se marcará como 'Completado'. Esta acción no se puede deshacer.",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#28a745',
        cancelButtonColor: '#6c757d',
        confirmButtonText: 'Sí, ¡Completar!',
        cancelButtonText: 'Cancelar'
    }).then((result) => {
        if (result.isConfirmed) {
            // Si el usuario confirma, envía el formulario correspondiente
            document.getElementById('form-completar-' + tratamientoId).submit();
        }
    });
}

// Medicamento Admin
function confirmarEliminacionMedicamento(button) {
    // Obtener datos del btn (parámetro 'button' es el 'this' del HTML)
    const id = button.getAttribute('data-med-id');
    const nombre = button.getAttribute('data-med-nombre');

    // Mostrar alerta
    Swal.fire({
        title: '¿Estás seguro?',
        text: `¡El medicamento "${nombre}" será eliminado y no se podra revertir!`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Sí, ¡Eliminar!',
        cancelButtonText: 'Cancelar'
    }).then((result) => {
        if (result.isConfirmed) {
            // Buscar y enviar el formulario específico
            const form = document.getElementById('form-eliminar-medicamento-' + id);
            if (form) {
                form.submit();
            } else {
                console.error(`Error: No se encontró el formulario 'form-eliminar-medicamento-${id}'`);
            }
        }
    });
}

function confirmarCompletarActividad(btn) {
    const form = btn.closest('form');

    Swal.fire({
        title: '¿Completar actividad?',
        text: "¿Estás seguro de que deseas marcar esta actividad como completada?",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#198754',
        cancelButtonColor: '#6c757d',
        confirmButtonText: 'Sí, completar',
        cancelButtonText: 'Cancelar',
        reverseButtons: true
    }).then((result) => {
        if (result.isConfirmed) {
            // Si el usuario confirma, envia el formulario manualmente
            if (form) {
                form.submit();
            } else {
                console.error("No se encontró el formulario de la actividad.");
            }
        }
    });
}