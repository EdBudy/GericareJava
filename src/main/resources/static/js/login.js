/*Este archivo inicia las partículas.*/
document.addEventListener('DOMContentLoaded', function() {

    /* --- Configuración de Partículas del fondo (BLANCAS) --- */
    if(document.getElementById('particles-js-background')){
        particlesJS('particles-js-background', {
            "particles": {
                "number": { "value": 80, "density": { "enable": true, "value_area": 800 }},
                "color": { "value": "#ffffff" }, // Blancas
                "shape": { "type": "circle" },
                "opacity": { "value": 0.5, "random": false },
                "size": { "value": 3, "random": true },
                "line_linked": { "enable": true, "distance": 150, "color": "#ffffff", "opacity": 0.4, "width": 1 },
                "move": { "enable": true, "speed": 6, "direction": "none", "out_mode": "out", "bounce": false }
            },
            "interactivity": {
                "detect_on": "canvas",
                "events": { "onhover": { "enable": true, "mode": "repulse" }, "onclick": { "enable": true, "mode": "push" }, "resize": true },
                "modes": { "repulse": { "distance": 100, "duration": 0.4 }, "push": { "particles_nb": 4 } }
            },
            "retina_detect": true
        });
    }

    /* --- Configuración de Partículas de la tarjeta (Negro y Azul) --- */
    if(document.getElementById('particles-js-card')){
        particlesJS('particles-js-card', {
            "particles": {
                "number": { "value": 40, "density": { "enable": true, "value_area": 400 }},
                "color": {
                    "value": ["#007bff", "#343a40", "#0056b3", "#000000"]
                },
                "shape": { "type": "circle" },
                "opacity": { "value": 0.5, "random": true },
                "size": { "value": 4, "random": true },
                "line_linked": { "enable": false }, // Sin líneas
                "move": { "enable": true, "speed": 2, "direction": "top", "random": true, "out_mode": "out", "bounce": true }
            },
            "interactivity": { "detect_on": "canvas", "events": { "onhover": { "enable": false }, "onclick": { "enable": false }, "resize": true }},
            "retina_detect": true
        });
    }
});