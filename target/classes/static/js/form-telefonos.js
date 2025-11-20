document.addEventListener('DOMContentLoaded', function() {
    const addButton = document.getElementById('add-telefono');
    if (!addButton) return; // Si el botón no existe en la página, no hacer nada.

    const wrapper = document.getElementById('telefonos-wrapper');

    // Función para actualizar la visibilidad del botón "Añadir"
    const updateAddButtonVisibility = () => {
        const count = wrapper.getElementsByClassName('input-group').length;
        if (count >= 3) {
            addButton.style.display = 'none';
        } else {
            addButton.style.display = 'block';
        }
    };

    addButton.addEventListener('click', function() {
        const count = wrapper.getElementsByClassName('input-group').length;

        if (count < 3) {
            const newField = document.createElement('div');
            newField.className = 'input-group mb-2';

            // El name del input (telefonos[x].numero o telefonos[x]) se adapta a lo que Thymeleaf espera
            const inputName = wrapper.dataset.formType === 'admin' ? `telefonos[${count}]` : `telefonos[${count}].numero`;

            newField.innerHTML = `
                <input type="text" name="${inputName}" class="form-control" pattern="[0-9]+" title="Este campo solo admite números." placeholder="Número de teléfono (opcional)" />
                <button type="button" class="btn btn-outline-danger btn-sm">Eliminar</button>
            `;

            // Añadir evento de eliminación al nuevo botón
            newField.querySelector('button').addEventListener('click', function() {
                newField.remove();
                updateAddButtonVisibility(); // Actualizar visibilidad al eliminar
            });

            wrapper.appendChild(newField);
            updateAddButtonVisibility(); // Actualizar visibilidad al añadir
        }
    });

    // Llamada inicial para asegurar que el estado del botón es correcto al cargar la página
    updateAddButtonVisibility();
});