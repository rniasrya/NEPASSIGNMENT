const baseUrl = 'http://localhost:8080';

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
        console.log(`Coffee Maker current status:\n\nID: ${data.id}\nState: ${data.state}\nBrew Strength: ${data.brewStrength}\nTemperature: ${data.temperature}°C\nBrew Time: ${data.brewTime} seconds`);
    })
    .catch(error => {
        console.error('Error:', error);
        alert(`${error.message}`);
    });
}

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
    .catch(error => alert(error.message)); 
}

/*function refillCoffeeMaker() {
    fetch(`${baseUrl}/coffeeMaker/refill`, {
        method: 'POST',
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(err => {
                throw new Error(err.message || 'Failed to stop refill Coffee Maker');
            });
        }
        return console.log('Coffee Maker refilled');
    })
    .catch(error => alert(error.message));
}*/

function refillWaterResource() {
    fetch(`${baseUrl}/coffeeMaker/refillWater`, {
        method: 'POST',
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(err => {
                throw new Error(err.message || 'Failed to stop refill Coffee Maker');
            });
        }
        return console.log('Coffee Maker refilled');
    })
    .catch(error => alert(error.message));
}	

function refillCoffeeGroundsResource() {
    fetch(`${baseUrl}/coffeeMaker/refillCoffeeGrounds`, {
        method: 'POST',
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(err => {
                throw new Error(err.message || 'Failed to stop refill Coffee Maker');
            });
        }
        return console.log('Coffee Maker refilled');
    })
    .catch(error => alert(error.message));
}	

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
        console.log(`Mixer current status:\n\nID: ${data.id}\nState: ${data.state}\nSpeed Level: ${data.speedLevel}`);
    })
    .catch(error => alert(`${error.message}`));
}

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
        console.log(`Microwave current status:\n\nID: ${data.id}\nState: ${data.state}\nTemperature: ${data.temperature}°C\nTimer: ${data.timer} mins`);
    })
    .catch(error => alert(`${error.message}`));
}

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
        console.log(`Rice Cooker current status:\n\nID: ${data.id}\nState: ${data.state}\nMode: ${data.mode}`);
    })
    .catch(error => alert(`${error.message}`));
}

function stopRiceCooker() {
    const id = 1; 
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
		document.getElementById('ricecooker-timer').textContent = `Rice Cooker Timer: ${message.body}`;
	});
	
	// Subscribe to microwave timer updates
	stompClient.subscribe('/topic/mixerTimer', function (message) {
		document.getElementById('mixer-timer').textContent = `Mixer Timer: ${message.body}`;
	});
	
	stompClient.subscribe('/topic/microwaveTemperature', function (message) {
	    // Parse the temperature value from the message body, then format it to 2 decimal places
	    let temperature = parseFloat(message.body).toFixed(0);
	    document.getElementById('microwave-temp').textContent = `Microwave Temperature: ${temperature}°C`;
	});
	
	stompClient.subscribe('/topic/riceCookerTemperature', function (message) {
		    // Parse the temperature value from the message body, then format it to 2 decimal places
		    let temperature = parseFloat(message.body).toFixed(0);
		    document.getElementById('ricecooker-temp').textContent = `Rice Cooker Temperature: ${temperature}°C`;
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
	
	// Coffee Maker Usage Count
	stompClient.subscribe('/topic/coffeeMakerUsage', function (message) {
		var usageCount = message.body;
	    document.getElementById('CoffeeMakerUsageCount').innerText = 'Usage Count: ' + usageCount;
	});	
	
	// Microwave Usage Count
	stompClient.subscribe('/topic/microwaveUsage', function (message) {
		var usageCount = message.body;
	    document.getElementById('MicrowaveUsageCount').innerText = 'Usage Count: ' + usageCount;
	});	
	
	// Mixer Usage Count
	stompClient.subscribe('/topic/mixerUsage', function (message) {
		var usageCount = message.body;
	    document.getElementById('MixerUsageCount').innerText = 'Usage Count: ' + usageCount;
	});	
	
	// Rice Cooker Usage Count
	stompClient.subscribe('/topic/riceCookerUsage', function (message) {
		var usageCount = message.body;
	    document.getElementById('RiceCookerUsageCount').innerText = 'Usage Count: ' + usageCount;
	});	
	
	// Coffee Maker Available Resource Count
	stompClient.subscribe('/topic/coffeeMakerWaterResource', function (message) {
		var resourceStatus = message.body;
	    document.getElementById('CoffeeMakerWaterResource').innerText = 'Water Level: ' + resourceStatus;
	});	
	
	// Rice Cooker Usage Count
	stompClient.subscribe('/topic/coffeeMakerCGResource', function (message) {
		var resourceStatus = message.body;
	    document.getElementById('CoffeeMakerCGResource').innerText = 'Coffee Ground: ' + resourceStatus;
	});	
	
	stompClient.subscribe('/topic/motorSpeed', function(message) {
	     var motorSpeed = parseInt(message.body);
	     document.getElementById('motor-speed-display').textContent = `Motor Speed: ${motorSpeed} RPM`;
	});	
	
});

// Request current state (optional)
function requestApplianceState(appliance) {
    stompClient.send(`/app/${appliance}`, {}, {});
}

// Function to show the popup
function showPopup() {
    document.getElementById("popupOverlay").style.display = "flex";
}

// Function to hide the popup
function hidePopup() {
    document.getElementById("popupOverlay").style.display = "none";
}


