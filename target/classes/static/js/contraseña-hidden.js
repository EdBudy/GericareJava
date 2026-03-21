document.addEventListener('DOMContentLoaded', function() {
    // Selecciona todos los botones para mostrar/ocultar contraseña
    const togglePasswordButtons = document.querySelectorAll('.toggle-password-btn');

    togglePasswordButtons.forEach(button => {
        button.addEventListener('click', function () {
            // Encuentra el campo de contraseña asociado a este botón
            // Sube al 'input-group' y busca el input dentro de él.
            const passwordInput = this.closest('.input-group').querySelector('input');

            // Cambia el tipo de input
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);

            // Cambia el ícono del ojo
            const icon = this.querySelector('i');
            if (type === 'password') {
                icon.classList.remove('bi-eye-fill');
                icon.classList.add('bi-eye-slash-fill');
            } else {
                icon.classList.remove('bi-eye-slash-fill');
                icon.classList.add('bi-eye-fill');
            }
        });
    });
});