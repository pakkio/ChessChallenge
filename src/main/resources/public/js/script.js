document.addEventListener('DOMContentLoaded', (event) => {
    fetch('/api/application')
        .then(response => response.json())
        .then(data => {
            const appElement = document.getElementById('app');
            appElement.innerHTML = `<h1>${data.name}</h1><p>${data.description}</p>`;
        })
        .catch(error => console.error('Error:', error));
});