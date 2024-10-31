const baseUrl = 'http://localhost:8080';

/*function startCoffeeMaker() {
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
}*/

/*function startCoffeeMaker() {
    fetch(`${baseUrl}/coffeeMaker/start`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({})
    })
    .then(response => {
        if (!response.ok) { // If response is not ok, the appliance couldn’t start
            return response.text().then(text => { throw new Error(text); });
        }
        return response.json();
    })
    .then(data => {
        alert(`Coffee Maker started!\n\nID: ${data.id}\nState: ${data.state}\nTemperature: ${data.temperature}°C\nBrew Time: ${data.brewTime} mins`);
    })
    .catch(error => alert(`Error starting Coffee Maker: ${error.message}`));
}*/

function startCoffeeMaker() {
	const brewStrength = document.getElementById('brewStrength').value;
	
    fetch(`${baseUrl}/coffeeMaker/start?brewStrength=${brewStrength}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({})
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => { throw new Error(text); });
        }
        return response.json();
    })
    .then(data => {
        alert(`Coffee Maker started!\n\nID: ${data.id}\nState: ${data.state}\nBrew Strength: ${data.brewStrength}\nTemperature: ${data.temperature}°C\nBrew Time: ${data.brewTime} mins`);
    })
    .catch(error => {
        console.error('Error:', error);
        alert(`${error.message}`);
    });
}

/*function stopCoffeeMaker() {
    const id = 1; // Replace with the actual ID you want to stop
    fetch(`${baseUrl}/coffeeMaker/stop/${id}`, {
        method: 'POST',
    })
    .then(() => alert('Coffee Maker stopped'))
    .catch(error => console.error('Error:', error));
}*/

function stopCoffeeMaker() {
    const id = 1; // Replace with the actual ID you want to stop
    fetch(`${baseUrl}/coffeeMaker/stop/${id}`, {
        method: 'POST',
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(err => {
                throw new Error(err.message || 'Failed to stop Coffee Maker');
            });
        }
        return console.log('Coffee Maker stopped');
    })
    .catch(error => alert(error.message)); // Display the error message to the user
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
	
	if (!speedLevel) {
	        alert('Please set the Mixer speed level.');
	        return; // Exit the function if the speed level is not set
	    }
	
    fetch(`${baseUrl}/mixer/start?speedLevel=${speedLevel}`, {
        method: 'POST',
    })
	.then(response => {
	        if (!response.ok) {
	            return response.text().then(text => {
	                throw new Error(text);
	            });
	        }
	        return response.json();
	})		
    .then(data => {
        alert(`Mixer started!\n\nID: ${data.id}\nState: ${data.state}\nSpeed Level: ${data.speedLevel}`);
    })
    .catch(error => alert(`${error.message}`));
}

/*function stopMixer() {
    const id = 1; // Replace with the actual ID you want to stop
    fetch(`${baseUrl}/mixer/stop/${id}`, {
        method: 'POST',
    })
    .then(() => alert('Mixer stopped'))
    .catch(error => console.error('Error:', error));
}*/

function stopMixer() {
    const id = 1; // Replace with the actual ID you want to stop
	
    fetch(`${baseUrl}/mixer/stop/${id}`, {
        method: 'POST',
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(err => {
                throw new Error(err.message || 'Failed to stop Mixer');
            });
        }
        return console.log('Mixer has stopped');
    })
    .catch(error => alert(error.message));
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
	
	if (!temperature && !timer) {
	        alert('Please set both the Microwave temperature and timer.');
			return;
		} else if (!temperature){
			alert('Please set the Microwave temperature.');
			return;
		} else if (!timer) {
			alert('Please set the Microwave timer.');
	        return; // Exit the function if any field is empty	
	    }
	
    fetch(`${baseUrl}/microwave/start?temperature=${temperature}&timer=${timer}`, {
        method: 'POST',
		})
		    .then(response => {
		        if (!response.ok) {
		            return response.text().then(text => {
		                throw new Error(text);
		            });
		        }
		        return response.json();
	})
    .then(data => {
        alert(`Microwave started!\n\nID: ${data.id}\nState: ${data.state}\nTemperature: ${data.temperature}°C\nTimer: ${data.timer} mins`);
    })
    .catch(error => alert(`${error.message}`));
}

/*function stopMicrowave() {
    const id = 1; // Replace with the actual ID you want to stop
    fetch(`${baseUrl}/microwave/stop/${id}`, {
        method: 'POST',
    })
    .then(() => alert('Microwave stopped'))
    .catch(error => console.error('Error:', error));
}*/

function stopMicrowave() {
    const id = 1; // Replace with the actual ID you want to stop
    fetch(`${baseUrl}/microwave/stop/${id}`, {
        method: 'POST',
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(err => {
                throw new Error(err.message || 'Failed to stop Microwave');
            });
        }
        return console.log('Microwave has stopped.');
    })
    .catch(error => alert(error.message));
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
	
	if (!mode) {
	        alert('Please select a mode for the Rice Cooker.');
	        return; // Exit the function if no mode is selected
	    }
	
    fetch(`${baseUrl}/riceCooker/start?mode=${mode}`, {
        method: 'POST',
		})
		    .then(response => {
		        if (!response.ok) {
		            return response.text().then(text => {
		                throw new Error(text);
		            });
		        }
		        return response.json();
		    })
    .then(data => {
        alert(`Rice Cooker started!\n\nID: ${data.id}\nState: ${data.state}\nMode: ${data.mode}`);
    })
    .catch(error => alert(`${error.message}`));
}

/*function stopRiceCooker() {
    const id = 1; 
    fetch(`${baseUrl}/riceCooker/stop/${id}`, {
        method: 'POST',
    })
    .then(() => alert('Rice Cooker stopped'))
    .catch(error => console.error('Error:', error));
}*/

function stopRiceCooker() {
    const id = 1; // Replace with the actual ID you want to stop
    fetch(`${baseUrl}/riceCooker/stop/${id}`, {
        method: 'POST',
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(err => {
                throw new Error(err.message || 'Failed to stop Rice Cooker');
            });
        }
        return console.log('Rice Cooker has stopped.');
    })
    .catch(error => alert(error.message));
}

// ===================

// WebSocket integration
var socket = new SockJS('/ws');
var stompClient = Stomp.over(socket);

stompClient.connect({}, function (frame) {
    console.log('Connected: ' + frame);
	
	 stompClient.subscribe('/topic/coffeeBrewingTimer', function (message) {
	     document.getElementById('brewing-timer').textContent = `Brewing Timer: ${message.body}`;
	 });
	
	// Subscribe to microwave timer updates
	stompClient.subscribe('/topic/microwaveTimer', function (message) {
	    const remainingTimeFormatted = message.body;
	    document.getElementById('microwave-timer').textContent = `Microwave Timer: ${remainingTimeFormatted}`;
	});
	
	// Subscribe to microwave timer updates
	stompClient.subscribe('/topic/riceCookerTimer', function (message) {
		document.getElementById('ricecooker-timer').textContent = `Ricecooker Timer: ${message.body}`;
	});
	
	// Subscribe to microwave timer updates
	stompClient.subscribe('/topic/mixerTimer', function (message) {
		document.getElementById('mixer-timer').textContent = `Mixer Timer: ${message.body}`;
	});
	
	// Subscribe to appliance status updates
	stompClient.subscribe('/topic/applianceStatus', function (message) {
	    var notification = message.body;
	    alert(notification); // Show notification to the user
	});

    // Coffee Maker
    stompClient.subscribe('/topic/coffeeMaker', function (message) {
		console.log('Coffee Maker Message:', message.body);
        var coffeeMaker = JSON.parse(message.body);
        document.getElementById('coffeeMakerStatus').innerHTML = 'Coffee Maker State: ' + coffeeMaker.state;
    });
    
    // Mixer
    stompClient.subscribe('/topic/mixer', function (message) {
        var mixer = JSON.parse(message.body);
        document.getElementById('mixerStatus').innerHTML = 'Mixer State: ' + mixer.state;
    });

	// Microwave
    stompClient.subscribe('/topic/microwave', function (message) {
        var microwave = JSON.parse(message.body);
        document.getElementById('microwaveStatus').innerHTML = 'Microwave State: ' + microwave.state;
    });

	// Rice Cooker
    stompClient.subscribe('/topic/riceCooker', function (message) {
        var riceCooker = JSON.parse(message.body);
        document.getElementById('riceCookerStatus').innerHTML = 'Rice Cooker State: ' + riceCooker.state;
    });
});

// Request current state (optional)
function requestApplianceState(appliance) {
    stompClient.send(`/app/${appliance}`, {}, {});
}
