document.addEventListener('DOMContentLoaded', function() {

    const fechaInicioInput = document.getElementById('fechaInicio');
    const fechaFinInput = document.getElementById('fechaFin');

    const hoy = new Date().toISOString().split('T')[0];

    fechaInicioInput.setAttribute('min', hoy);
    fechaFinInput.setAttribute('min', hoy);

    fechaInicioInput.addEventListener('change', function() {
        const fechaInicioValor = fechaInicioInput.value;

        if (fechaInicioValor) {
            fechaFinInput.setAttribute('min', fechaInicioValor);

            if (fechaFinInput.value && fechaFinInput.value < fechaInicioValor) {
                fechaFinInput.value = '';
            }
        } else {
            fechaFinInput.setAttribute('min', hoy);
        }
    });

    if (fechaInicioInput.value) {
        fechaFinInput.setAttribute('min', fechaInicioInput.value);
    }

});