document.addEventListener('DOMContentLoaded', () => {


    // Fondo principal (Blanco/Gris sutil)
    if (document.getElementById('particles-js-background')) {
        particlesJS('particles-js-background', {
            "particles": {
                "number": { "value": 60, "density": { "enable": true, "value_area": 800 } },
                "color": { "value": "#ffffff" },
                "shape": { "type": "circle" },
                "opacity": { "value": 0.3, "random": false },
                "size": { "value": 3, "random": true },
                "line_linked": { "enable": true, "distance": 150, "color": "#ffffff", "opacity": 0.2, "width": 1 },
                "move": { "enable": true, "speed": 3, "direction": "none", "out_mode": "out" }
            },
            "interactivity": { "events": { "onhover": { "enable": false }, "onclick": { "enable": false } } }
        });
    }

    // Fondo de la tarjeta (Azules y Morados, movimiento hacia arriba)
    if (document.getElementById('particles-js-card')) {
        particlesJS('particles-js-card', {
            "particles": {
                "number": { "value": 30, "density": { "enable": true, "value_area": 400 } },
                "color": { "value": ["#007bff", "#6200ea"] },
                "shape": { "type": "circle" },
                "opacity": { "value": 0.5, "random": true },
                "size": { "value": 4, "random": true },
                "line_linked": { "enable": false },
                "move": { "enable": true, "speed": 1, "direction": "top", "out_mode": "out" }
            },
            "interactivity": { "events": { "onhover": { "enable": false }, "onclick": { "enable": false } } }
        });
    }


    const passwordInput = document.getElementById('password');
    const confirmInput = document.getElementById('confirmPassword');
    const toggleButtons = document.querySelectorAll('.toggle-password');
    const strengthBar = document.getElementById('strength-bar');
    const strengthText = document.getElementById('strength-text');
    const matchFeedback = document.getElementById('match-feedback');
    const submitBtn = document.getElementById('btn-submit');

    // Mostrar/Ocultar Contraseña 
    toggleButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const targetId = btn.getAttribute('data-target');
            const input = document.getElementById(targetId);
            const icon = btn.querySelector('i');

            if (input.type === 'password') {
                input.type = 'text';
                icon.classList.remove('bi-eye-slash');
                icon.classList.add('bi-eye');
                btn.classList.add('text-primary'); // Highlight activo
            } else {
                input.type = 'password';
                icon.classList.remove('bi-eye');
                icon.classList.add('bi-eye-slash');
                btn.classList.remove('text-primary');
            }
        });
    });

    //   Cálculo de Fuerza de Contraseña 
    function calculateStrength(password) {
        let strength = 0;

        if (password.length >= 8) strength += 25;
        if (password.match(/[a-z]+/)) strength += 25;
        if (password.match(/[A-Z]+/)) strength += 25;
        if (password.match(/[0-9]+/) || password.match(/[$@#&!]+/)) strength += 25;

        return strength;
    }

    function updateStrengthMeter() {
        const val = passwordInput.value;
        const strength = calculateStrength(val);

        // Ancho de la barra
        strengthBar.style.width = strength + '%';

        // Color y Texto
        if (strength <= 25) {
            strengthBar.className = 'progress-bar bg-danger';
            strengthText.textContent = 'Contraseña muy débil (*OBLIGATORIO* mínimo 8 caracteres)';
            strengthText.className = 'text-danger small fw-bold';
        } else if (strength <= 50) {
            strengthBar.className = 'progress-bar bg-warning';
            strengthText.textContent = 'Contraseña débil - Se recomienda Añadir mayúsculas';
            strengthText.className = 'text-warning small fw-bold';
        } else if (strength <= 75) {
            strengthBar.className = 'progress-bar bg-info';
            strengthText.textContent = 'Contraseña buena - Se recomienda añadir números/símbolos';
            strengthText.className = 'text-info small fw-bold';
        } else {
            strengthBar.className = 'progress-bar bg-success';
            strengthText.textContent = '¡Excelente! Contraseña segura.';
            strengthText.className = 'text-success small fw-bold';
        }
    }

    //   Validación de Coincidencia 
    function checkMatch() {
        const pass = passwordInput.value;
        const confirm = confirmInput.value;

        if (confirm === '') {
            confirmInput.classList.remove('is-invalid-custom', 'is-valid-custom');
            matchFeedback.classList.add('d-none');
            return false;
        }

        if (pass === confirm && pass.length > 0) {
            confirmInput.classList.remove('is-invalid-custom');
            confirmInput.classList.add('is-valid-custom'); // Borde Verde
            matchFeedback.classList.add('d-none');
            return true;
        } else {
            confirmInput.classList.remove('is-valid-custom');
            confirmInput.classList.add('is-invalid-custom'); // Borde Rojo
            matchFeedback.classList.remove('d-none');
            return false;
        }
    }

    // Validación Global para el Botón
    function validateForm() {
        const isStrongEnough = calculateStrength(passwordInput.value) >= 50; // Al menos "Débil" (longitud + letras)
        const isMatch = passwordInput.value === confirmInput.value && confirmInput.value !== '';

        if (isStrongEnough && isMatch) {
            submitBtn.disabled = false;
        } else {
            submitBtn.disabled = true;
        }
    }

    //  Listeners 
    passwordInput.addEventListener('input', () => {
        updateStrengthMeter();
        if(confirmInput.value !== '') checkMatch(); // Re-validar coincidencia si cambia la original
        validateForm();
    });

    confirmInput.addEventListener('input', () => {
        checkMatch();
        validateForm();
    });
});