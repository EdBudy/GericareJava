document.addEventListener('DOMContentLoaded', function() {

    // Función de "debounce" para no saturar al servidor con cada tecla
    // Espera 'delay' ms después de la última vez que el usuario tecleó
    function debounce(func, delay) {
        let timeout;
        return function(...args) {
            const context = this;
            clearTimeout(timeout);
            timeout = setTimeout(() => func.apply(context, args), delay);
        };
    }

    const formUsuarios = document.getElementById('filtro-form-usuarios');
    const tablaBodyUsuarios = document.getElementById('tabla-usuarios-body');

    // Solo ejecutar si esta en la página que contiene estos elementos
    if (formUsuarios && tablaBodyUsuarios) {

        const inputs = formUsuarios.querySelectorAll('input, select');
        const urlBase = formUsuarios.action; // Obtiene la URL del th:action (@{/dashboard})

        const actualizarTablaUsuarios = async () => {
            const params = new URLSearchParams(new FormData(formUsuarios));

            try {
                // Llama al controlador (DashboardController) con los parámetros de filtro
                const response = await fetch(`${urlBase}?${params.toString()}`);
                if (!response.ok) {
                    throw new Error('Error en la respuesta del servidor');
                }

                const htmlText = await response.text();

                // Parsea el HTML de la página completa que devolvió el servidor
                const parser = new DOMParser();
                const doc = parser.parseFromString(htmlText, 'text/html');

                // Busca el <tbody> de usuarios en la nueva página
                const nuevoTablaBody = doc.getElementById('tabla-usuarios-body');

                if (nuevoTablaBody) {
                    // Reemplaza el contenido de la tabla actual con el nuevo
                    tablaBodyUsuarios.innerHTML = nuevoTablaBody.innerHTML;
                }

            } catch (error) {
                console.error('Error al actualizar la tabla de usuarios:', error);
            }
        };

        // Asigna el evento 'input' (cada vez que se teclea o cambia)
        // Usa debounce para esperar 300ms antes de llamar
        inputs.forEach(input => {
            input.addEventListener('input', debounce(actualizarTablaUsuarios, 300));
        });
    }

    // Lógica para Pacientes

    const formPacientes = document.getElementById('filtro-form-pacientes');
    const tablaBodyPacientes = document.getElementById('tabla-pacientes-body');

    // Solo ejecutar si estamos en la página que contiene estos elementos
    if (formPacientes && tablaBodyPacientes) {

        const inputsPacientes = formPacientes.querySelectorAll('input, select');
        const urlBasePacientes = formPacientes.action; // Obtiene la URL del th:action (@{/pacientes})

        const actualizarTablaPacientes = async () => {
            const params = new URLSearchParams(new FormData(formPacientes));

            try {
                // Llama al PacienteController con los parámetros de filtro
                const response = await fetch(`${urlBasePacientes}?${params.toString()}`);
                if (!response.ok) {
                    throw new Error('Error en la respuesta del servidor');
                }

                const htmlText = await response.text();

                // Parsea el HTML de la página completa
                const parser = new DOMParser();
                const doc = parser.parseFromString(htmlText, 'text/html');

                // Busca el <tbody> de pacientes en la *nueva* página
                const nuevoTablaBody = doc.getElementById('tabla-pacientes-body');

                if (nuevoTablaBody) {
                    // Reemplaza el contenido de la tabla actual con el nuevo
                    tablaBodyPacientes.innerHTML = nuevoTablaBody.innerHTML;
                }

            } catch (error) {
                console.error('Error al actualizar la tabla de pacientes:', error);
            }
        };

        // Asigna el evento 'input' (cada vez que se teclea o cambia)
        // Usa el mismo debounce de 300ms
        inputsPacientes.forEach(input => {
            input.addEventListener('input', debounce(actualizarTablaPacientes, 300));
        });
    }

});