const baseUrl = 'http://localhost:8080';

function startAppliance(appliance, params = {}) {
    fetch(`${baseUrl}/${appliance}/start${params ? '?' + new URLSearchParams(params).toString() : ''}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
    })
    .then(response => response.json())
    .then(data => alert(`${appliance.charAt(0).toUpperCase() + appliance.slice(1)} started: ${JSON.stringify(data)}`))
    .catch(error => console.error('Error:', error));
}

function stopAppliance(appliance, id) {
    fetch(`${baseUrl}/${appliance}/stop/${id}`, {
        method: 'POST',
    })
    .then(() => alert(`${appliance.charAt(0).toUpperCase() + appliance.slice(1)} stopped`))
    .catch(error => console.error('Error:', error));
}

function startCoffeeMaker() {
    fetch(`${baseUrl}/coffeeMaker/start`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({})
    })
    .then(response => response.json())
    .then(data => {
        alert(`Coffee Maker started!\n\nID: ${data.id}\nState: ${data.state}\nTemperature: ${data.temperature}°C\nBrew Time: ${data.brewTime} mins`);
    })
    .catch(error => console.error('Error:', error));
}

function stopCoffeeMaker() {
    const id = 1; // Replace with the actual ID you want to stop
    fetch(`${baseUrl}/coffeemaker/stop/${id}`, {
        method: 'POST',
    })
    .then(() => alert('Coffee Maker stopped'))
    .catch(error => console.error('Error:', error));
}

/* function startMixer() {
    const speedLevel = document.getElementById('mixerSpeed').value;
    fetch(`${baseUrl}/mixer/start?speedLevel=${speedLevel}`, {
        method: 'POST',
    })
    .then(response => response.json())
    .then(data => alert(`Mixer started: ${JSON.stringify(data)}`))
    .catch(error => console.error('Error:', error));
} */

function startMixer() {
    const speedLevel = document.getElementById('mixerSpeed').value;
    fetch(`${baseUrl}/mixer/start?speedLevel=${speedLevel}`, {
        method: 'POST',
    })
    .then(response => response.json())
    .then(data => {
        alert(`Mixer started!\n\nID: ${data.id}\nState: ${data.state}\nSpeed Level: ${data.speedLevel}`);
    })
    .catch(error => console.error('Error:', error));
}

function stopMixer() {
    const id = 1; // Replace with the actual ID you want to stop
    fetch(`${baseUrl}/mixer/stop/${id}`, {
        method: 'POST',
    })
    .then(() => alert('Mixer stopped'))
    .catch(error => console.error('Error:', error));
}

/* function startMicrowave() {
    const temperature = document.getElementById('microwaveTemperature').value;
    const timer = document.getElementById('microwaveTimer').value;
    fetch(`${baseUrl}/microwave/start?temperature=${temperature}&timer=${timer}`, {
        method: 'POST',
    })
    .then(response => response.json())
    .then(data => alert(`Microwave started: ${JSON.stringify(data)}`))
    .catch(error => console.error('Error:', error));
} */

function startMicrowave() {
    const temperature = document.getElementById('microwaveTemperature').value;
    const timer = document.getElementById('microwaveTimer').value;
    fetch(`${baseUrl}/microwave/start?temperature=${temperature}&timer=${timer}`, {
        method: 'POST',
    })
    .then(response => response.json())
    .then(data => {
        alert(`Microwave started!\n\nID: ${data.id}\nState: ${data.state}\nTemperature: ${data.temperature}°C\nTimer: ${data.timer} mins`);
    })
    .catch(error => console.error('Error:', error));
}

function stopMicrowave() {
    const id = 1; // Replace with the actual ID you want to stop
    fetch(`${baseUrl}/microwave/stop/${id}`, {
        method: 'POST',
    })
    .then(() => alert('Microwave stopped'))
    .catch(error => console.error('Error:', error));
}

/* function startRiceCooker() {
    const mode = document.getElementById('riceCookerMode').value;
    fetch(`${baseUrl}/ricecooker/start?mode=${mode}`, {
        method: 'POST',
    })
    .then(response => response.json())
    .then(data => alert(`Rice Cooker started: ${JSON.stringify(data)}`))
    .catch(error => console.error('Error:', error));
} */

function startRiceCooker() {
    const mode = document.getElementById('riceCookerMode').value;
    fetch(`${baseUrl}/ricecooker/start?mode=${mode}`, {
        method: 'POST',
    })
    .then(response => response.json())
    .then(data => {
        alert(`Rice Cooker started!\n\nID: ${data.id}\nState: ${data.state}\nMode: ${data.mode}`);
    })
    .catch(error => console.error('Error:', error));
}

function stopRiceCooker() {
    const id = 1; 
    fetch(`${baseUrl}/ricecooker/stop/${id}`, {
        method: 'POST',
    })
    .then(() => alert('Rice Cooker stopped'))
    .catch(error => console.error('Error:', error));
}

// ===================

// WebSocket integration
var socket = new SockJS('/ws');
var stompClient = Stomp.over(socket);

stompClient.connect({}, function (frame) {
    console.log('Connected: ' + frame);

    // Subscribe to topics for all appliances
    stompClient.subscribe('/topic/coffeeMaker', function (message) {
        var coffeeMaker = JSON.parse(message.body);
        document.getElementById('coffeeMakerStatus').innerHTML = 'Coffee Maker State: ' + coffeeMaker.state;
    });
    
    // Add similar subscriptions for other appliances
    stompClient.subscribe('/topic/mixer', function (message) {
        var mixer = JSON.parse(message.body);
        document.getElementById('mixerStatus').innerHTML = 'Mixer State: ' + mixer.state;
    });

    stompClient.subscribe('/topic/microwave', function (message) {
        var microwave = JSON.parse(message.body);
        document.getElementById('microwaveStatus').innerHTML = 'Microwave State: ' + microwave.state;
    });

    stompClient.subscribe('/topic/riceCooker', function (message) {
        var riceCooker = JSON.parse(message.body);
        document.getElementById('riceCookerStatus').innerHTML = 'Rice Cooker State: ' + riceCooker.state;
    });
});

// Request current state (optional)
function requestApplianceState(appliance) {
    stompClient.send(`/app/${appliance}`, {}, {});
}

// Call this function periodically to fetch the latest state for Coffee Maker
// setInterval(() => requestApplianceState('coffeeMaker'), 5000); // Fetch every 5 seconds