const form = document.getElementById('detailsForm');
const result = document.getElementById('result');

// Show an error message under a field
function showError(field, message) {
    const errorEl = document.querySelector(`[data-error-for="${field}"]`);
    if (errorEl) {
        errorEl.textContent = message;
        errorEl.classList.remove('hidden');
    }
}

// Clear all error messages
function clearErrors() {
    document.querySelectorAll('[data-error-for]').forEach(el => {
        el.textContent = '';
        el.classList.add('hidden');
    });
    result.classList.add('hidden');
}

function validate(data) {
    let valid = true;

    if (!data.name.trim()) {
        showError('name', 'Name is required.');
        valid = false;
    }

    if (!data.age || Number(data.age) < 1 || Number(data.age) > 120) {
        showError('age', 'Enter a valid age between 1 and 120.');
        valid = false;
    }

    if (!data.dob) {
        showError('dob', 'Date of birth is required.');
        valid = false;
    }

    if (!data.gender) {
        showError('gender', 'Please select a gender.');
        valid = false;
    }

    const phonePattern = /^[+]?[\d\s()-]{7,15}$/;
    if (!phonePattern.test(data.phone)) {
        showError('phone', 'Enter a valid phone number.');
        valid = false;
    }

    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailPattern.test(data.email)) {
        showError('email', 'Enter a valid email address.');
        valid = false;
    }

    if (!data.address.trim()) {
        showError('address', 'Address is required.');
        valid = false;
    }

    return valid;
}

form.addEventListener('submit', (event) => {
    event.preventDefault();
    clearErrors();

    const data = {
        name: form.name.value,
        age: form.age.value,
        dob: form.dob.value,
        gender: form.gender.value,
        phone: form.phone.value,
        email: form.email.value,
        address: form.address.value
    };

    if (!validate(data)) {
        return;
    }

    result.innerHTML = `
        <p class="font-medium text-green-200">Details submitted successfully!</p>
        <p class="mt-1">${data.name}, ${data.age} years, ${data.gender}</p>
        <p>${data.email} &middot; ${data.phone}</p>
    `;
    result.classList.remove('hidden');
    form.reset();
});

form.addEventListener('reset', () => {
    clearErrors();
});
