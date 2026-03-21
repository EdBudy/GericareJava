/*
 * Inicializa validador de festivos en un campo de fecha
 * @param {string} inputId - El ID del <input type="date">.
 * @param {string} alertaId - El ID del <span> donde se mostrará la alerta
 */
function inicializarValidadorFestivos(inputId, alertaId) {

    const inputFecha = document.getElementById(inputId);
    const alertaFestivo = document.getElementById(alertaId);

    // Si no encuentra los elementos, no hace nd
    if (!inputFecha || !alertaFestivo) {
        console.warn("Validador de festivos: No se encontraron los elementos", inputId, alertaId);
        return;
    }

    // Cache para guardar los festivos de un año y no llamar a la API varias veces
    let festivosCache = {};
    let ultimoAnioBuscado = null;

    // Cuando el usuario cambia la fecha
    inputFecha.addEventListener("change", function() {
        const fechaSeleccionada = this.value; // ej: "2025-07-20"
        if (!fechaSeleccionada) {
            alertaFestivo.textContent = "";
            return;
        }

        const anio = fechaSeleccionada.split("-")[0];

        // Revisar si ya se tiene los festivos de ese año
        if (anio !== ultimoAnioBuscado) {
            // Si es un año nuevo, llama a la API externa
            buscarFestivos(anio, fechaSeleccionada);
        } else {
            // Si ya los tiene, solo valida
            validarFecha(fechaSeleccionada);
        }
    });

    // Llamada a API
    function buscarFestivos(anio, fechaParaValidar) {
        alertaFestivo.textContent = "Consultando festivos...";

        // API externa: Es un GET público (sin llave ni nada)
        fetch('https://date.nager.at/api/v3/PublicHolidays/' + anio + '/CO')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error en la red al consultar festivos.');
                }
                return response.json();
            })
            .then(data => {
                // Guarda los datos en el caché
                festivosCache = {}; // Limpiamos caché anterior
                data.forEach(festivo => {
                    // Guarda la fecha (ej: "2025-07-20") y el nombre (ej: "Día de la Independencia")
                    festivosCache[festivo.date] = festivo.localName;
                });

                ultimoAnioBuscado = anio;

                // Valida la fecha que el usuario acaba de poner
                validarFecha(fechaParaValidar);
            })
            .catch(error => {
                console.error("Error consultando API de festivos:", error);
                alertaFestivo.textContent = "No se pudieron consultar festivos.";
            });
    }

    // Logica validación
    function validarFecha(fecha) {
        if (festivosCache[fecha]) {
            // Encontrado
            alertaFestivo.textContent = "Esta fecha es festivo: " + festivosCache[fecha];
        } else {
            // No es festivo
            alertaFestivo.textContent = "";
        }
    }
}