
document.addEventListener('DOMContentLoaded', () => {

    // --- 1. Cargar Partículas del Fondo ---
    if (document.getElementById('particles-js-background')) {
        particlesJS('particles-js-background', {
            "particles": {
                "number": { "value": 80, "density": { "enable": true, "value_area": 800 } },
                "color": { "value": "#ffffff" },
                "shape": { "type": "circle" },
                "opacity": { "value": 0.5, "random": false },
                "size": { "value": 3, "random": true },
                "line_linked": { "enable": true, "distance": 150, "color": "#ffffff", "opacity": 0.4, "width": 1 },
                "move": { "enable": true, "speed": 6, "direction": "none", "out_mode": "out" }
            },
            "interactivity": {
                "detect_on": "canvas",
                "events": { "onhover": { "enable": true, "mode": "repulse" }, "onclick": { "enable": true, "mode": "push" } },
                "modes": { "repulse": { "distance": 100 }, "push": { "particles_nb": 4 } }
            }
        });
    }

    // --- 2. Cargar Partículas de la Tarjeta (Azul y Morado) ---
    if (document.getElementById('particles-js-card')) {
        particlesJS('particles-js-card', {
            "particles": {
                "number": { "value": 40, "density": { "enable": true, "value_area": 400 } },
                "color": { "value": ["#007bff", "#6200ea"] }, // Azul y Morado
                "shape": { "type": "circle" },
                "opacity": { "value": 0.5, "random": true },
                "size": { "value": 4, "random": true },
                "line_linked": { "enable": false },
                "move": { "enable": true, "speed": 2, "direction": "top", "out_mode": "out" }
            },
            "interactivity": { "events": { "onhover": { "enable": false }, "onclick": { "enable": false } } }
        });
    }

    // --- 3. Lógica del Stepper (Formulario Multi-paso) ---
    const steps = document.querySelectorAll('.form-step');
    const nextButtons = document.querySelectorAll('.btn-next');
    const prevButtons = document.querySelectorAll('.btn-prev');
    const progressSteps = document.querySelectorAll('.progress-step');
    const progressLineActive = document.getElementById('progress-line-active');
    const form = document.getElementById('registro-form');

    let currentStep = 1;

    // Función para validar el paso actual
    function validateStep(stepIndex) {
        const step = steps[stepIndex - 1];
        if (!step) return false;

        const inputs = step.querySelectorAll('input[required], select[required]');
        let isValid = true;

        inputs.forEach(input => {
            // Quitar clases de validación anteriores
            input.classList.remove('is-invalid', 'is-valid');

            if (!input.value || (input.pattern && !new RegExp(input.pattern).test(input.value))) {
                isValid = false;
                input.classList.add('is-invalid'); // Marcar campo inválido
            } else {
                input.classList.add('is-valid');
            }
        });

        // Validación específica de teléfonos (asegurar al menos uno)
        if (stepIndex === 2) {
            const phoneInputs = step.querySelectorAll('#telefonos-wrapper input');
            if (phoneInputs.length === 0 || phoneInputs[0].value.trim() === '') {
                 isValid = false;
                 if (phoneInputs.length > 0) {
                     phoneInputs[0].classList.add('is-invalid');
                 }
            } else {
                 if (phoneInputs.length > 0) {
                     phoneInputs[0].classList.remove('is-invalid');
                     phoneInputs[0].classList.add('is-valid');
                 }
            }
        }

        return isValid;
    }

    // Función para actualizar la UI
    function updateUI() {
        // Ocultar todos los pasos
        steps.forEach(step => {
            step.classList.remove('active', 'animate__fadeIn');
        });

        // Mostrar el paso actual con animación
        const activeStep = document.querySelector(`.form-step[data-step="${currentStep}"]`);
        if(activeStep) {
            activeStep.classList.add('active', 'animate__fadeIn');
        }

        // Actualizar la barra de progreso
        progressSteps.forEach((step, index) => {
            if (index < currentStep) {
                step.classList.add('active');
            } else {
                step.classList.remove('active');
            }
        });

        // Actualizar la línea de progreso
        if (progressLineActive) {
            const activeSteps = document.querySelectorAll('.progress-step.active').length;
            const percent = ((activeSteps - 1) / (progressSteps.length - 1)) * 100;
            progressLineActive.style.width = percent + '%';
        }
    }

    // Event Listeners para "Siguiente"
    nextButtons.forEach(button => {
        button.addEventListener('click', () => {
            // Validar campos antes de pasar
            if (validateStep(currentStep)) {
                if (currentStep < steps.length) {
                    currentStep++;
                    updateUI();
                }
            }
        });
    });

    // Event Listeners para "Anterior"
    prevButtons.forEach(button => {
        button.addEventListener('click', () => {
            if (currentStep > 1) {
                currentStep--;
                updateUI();
            }
        });
    });

    // Evitar que "Enter" envíe el formulario antes del último paso
    if (form) {
        form.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && currentStep < steps.length) {
                e.preventDefault();
                // Opcional: simular clic en "Siguiente"
                nextButtons[currentStep - 1].click();
            }
        });
    }

    // Inicializar la UI
    updateUI();
});