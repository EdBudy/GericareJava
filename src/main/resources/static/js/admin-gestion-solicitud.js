/*ARCHIVAR (eliminar)*/
function confirmarArchivarSolicitudAdmin(solicitudId) {
    Swal.fire({
        title: '¿Archivar Solicitud?',
        text: "La solicitud se marcará como inactiva y no será visible. ¿Deseas continuar?",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#3085d6', // Azul confirmar acción
        cancelButtonColor: '#6c757d', // Gris cancelar
        confirmButtonText: 'Sí, ¡Archivar!',
        cancelButtonText: 'Cancelar'
    }).then((result) => {
        if (result.isConfirmed) {
             const formId = `form-archivar-solicitud-${solicitudId}`;
             let form = document.getElementById(formId);
             if (!form) {
                  // Lógica de fallback para encontrar el formulario si no tiene el ID esperado
                  form = document.querySelector(`form[action='/solicitudes/eliminar/${solicitudId}']`);
             }
             if(form){
                // Si el form no tiene ID asigna uno temporalmente si es necesario
                if (!form.id) form.id = `temp-form-eliminar-${solicitudId}`;
                document.getElementById(form.id).submit();
             } else {
                 console.error("Formulario de archivar no encontrado para solicitud ID:", solicitudId);
             }
        }
    });
}

/*APROBAR una solicitud.*/
function confirmarAprobar(solicitudId) {
    Swal.fire({
        title: '¿Aprobar Solicitud?',
        text: "La solicitud se marcará como 'Aprobada'. Esta acción no se puede deshacer.",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#28a745',
        cancelButtonColor: '#6c757d',
        confirmButtonText: 'Sí, ¡Aprobar!',
        cancelButtonText: 'Cancelar'
    }).then((result) => {
        if (result.isConfirmed) {
            document.getElementById('form-aprobar-' + solicitudId).submit();
        }
    });
}

/* rechazar una solicitud.*/
function confirmarRechazar(solicitudId) {
    Swal.fire({
        title: '¿Rechazar Solicitud?',
        text: "La solicitud se marcará como 'Rechazada'. Esta acción no se puede deshacer.",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#dc3545', // Rojo 'danger'
        cancelButtonColor: '#6c757d',
        confirmButtonText: 'Sí, ¡Rechazar!',
        cancelButtonText: 'Cancelar'
    }).then((result) => {
        if (result.isConfirmed) {
            document.getElementById('form-rechazar-' + solicitudId).submit();
        }
    });
}