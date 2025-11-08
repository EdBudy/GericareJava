/*
 * JS PARA EL LAYOUT PRINCIPAL (MAIN.JS)
 */
document.addEventListener('DOMContentLoaded', function() {

    // --- Carga las partículas del Header ---
    if (document.getElementById('particles-js-header')) {
        particlesJS('particles-js-header', {
            "particles": {
                "number": { "value": 60, "density": { "enable": true, "value_area": 800 } },
                "color": { "value": ["#ffffff", "#007bff"] },
                "shape": { "type": "circle" },
                "opacity": { "value": 0.4, "random": true },
                "size": { "value": 3, "random": true },
                "line_linked": { "enable": true, "distance": 150, "color": "#ffffff", "opacity": 0.2, "width": 1 },
                "move": { "enable": true, "speed": 2, "direction": "none", "out_mode": "out" }
            },
            "interactivity": {
                "detect_on": "canvas",
                "events": { "onhover": { "enable": true, "mode": "grab" }, "onclick": { "enable": false } },
                "modes": { "grab": { "distance": 100, "line_linked": { "opacity": 0.5 } } }
            },
            "retina_detect": true
        });
    }

    // ---
    // NUEVO: Lógica para el botón flotante de Reportes (Stats)
    // ---
    const reportButton = document.getElementById('fab-report-button');
    if (reportButton) {
        reportButton.addEventListener('click', function() {

            Swal.fire({
                title: 'Generar Reporte',
                text: 'Selecciona el formato para tu reporte de usuarios:',
                icon: 'question',
                iconColor: '#007bff',
                showCancelButton: true,
                confirmButtonText: '<i class="bi bi-file-earmark-excel-fill me-2"></i>Exportar Excel',
                cancelButtonText: '<i class="bi bi-file-earmark-pdf-fill me-2"></i>Exportar PDF',
                confirmButtonColor: '#198754', // Verde (Excel)
                cancelButtonColor: '#dc3545', // Rojo (PDF)
                customClass: {
                    popup: 'swal-estetico' // Clase CSS para el fondo negro
                }
            }).then((result) => {
                if (result.isConfirmed) {
                    // Si hacen clic en "Excel"
                    // (Debes crear esta ruta en tu Controller)
                    window.location.href = '/admin/reporte/excel';

                } else if (result.dismiss === Swal.DismissReason.cancel) {
                    // Si hacen clic en "PDF"
                    // (Debes crear esta ruta en tu Controller)
                    window.location.href = '/admin/reporte/pdf';
                }
            });

        });
    }

});