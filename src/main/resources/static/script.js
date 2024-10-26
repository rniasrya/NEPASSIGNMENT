function startCoffeeMaker(brewTime) {
    fetch(`/api/appliances/coffee-maker/start?brewTime=${brewTime}`, {
        method: 'POST'
    })
    .then(response => response.text())
    .then(data => alert(data))
    .catch(error => console.error('Error:', error));
}

function stopCoffeeMaker() {
    fetch('/api/appliances/coffee-maker/stop', {
        method: 'POST'
    })
    .then(response => response.text())
    .then(data => alert(data))
    .catch(error => console.error('Error:', error));
}

function startRiceCooker() {
    fetch('/api/appliances/rice-cooker/start', {
        method: 'POST'
    })
    .then(response => response.text())
    .then(data => alert(data))
    .catch(error => console.error('Error:', error));
}

function stopRiceCooker() {
    fetch('/api/appliances/rice-cooker/stop', {
        method: 'POST'
    })
    .then(response => response.text())
    .then(data => alert(data))
    .catch(error => console.error('Error:', error));
}

function startMicrowave(time) {
    fetch(`/api/appliances/microwave/start?time=${time}`, {
        method: 'POST'
    })
    .then(response => response.text())
    .then(data => alert(data))
    .catch(error => console.error('Error:', error));
}

function stopMicrowave() {
    fetch('/api/appliances/microwave/stop', {
        method: 'POST'
    })
    .then(response => response.text())
    .then(data => alert(data))
    .catch(error => console.error('Error:', error));
}

function startMixer() {
    fetch('/api/appliances/mixer/start', {
        method: 'POST'
    })
    .then(response => response.text())
    .then(data => alert(data))
    .catch(error => console.error('Error:', error));
}

function stopMixer() {
    fetch('/api/appliances/mixer/stop', {
        method: 'POST'
    })
    .then(response => response.text())
    .then(data => alert(data))
    .catch(error => console.error('Error:', error));
}
