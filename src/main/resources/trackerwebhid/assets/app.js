setInterval(function(){
    axios.get("http://10.0.0.1:13000/api/state")
        .then(function (response) {
            render(response.data)
        })
        .catch(function (error) {
            document.getElementById("main-container").className = "container d-none";
            document.getElementById("error-container").className = "container d-block";
            console.log(error);
        });
}, 1000);

const signalData = [];
Chart.defaults.global.animation.duration = 0;

function fakeLabels(x) {
    const result = [];
    for (i = 0; i < x.length; i++) {
        result.push("");
    }

    return result;
}

function render(state) {
    document.getElementById("main-container").className = "container d-block";
    document.getElementById("error-container").className = "container d-none";

    if (signalData.length >= 300) {
        signalData.shift();
    }

    if (state.bandit_signal) {
        signalData.push(state.bandit_signal);
    } else {
        signalData.push(-100);
    }

    let leaderSignal = Math.round(state.leader_signal_strength/255*100);
    let connection = "DARK";
    let connectionClass = "indicator-color-red";

    if (state.tracker_state.includes("ONLINE")) {
        connection = "ONLINE";
        connectionClass = "indicator-color-green";
    }

    if (state.tracker_state.includes("WEAK")) {
        connection = "WEAK";
        connectionClass = "indicator-color-yellow";
    }

    // Connection status.
    document.getElementById("prop-connection").innerText = connection;
    document.getElementById("prop-connection").className = connectionClass;

    // Signal strength.
    if (connection === "DARK") {
        document.getElementById("prop-signal").innerText = "N/A";
        document.getElementById("prop-signal").className = "indicator-color-inactive";
    } else {
        document.getElementById("prop-signal").innerText = leaderSignal.toString() + "%";
        document.getElementById("prop-signal").className = connectionClass;
    }

    // Tracker device status.
    if (state.tracker_device_live) {
        document.getElementById("prop-link-device").innerText = "ONLINE";
        document.getElementById("prop-link-device").className = "indicator-color-green";
    } else {
        document.getElementById("prop-link-device").innerText = "OFFLINE";
        document.getElementById("prop-link-device").className = "indicator-color-red";
    }

    // 802.11 monitors status
    // Tracker device status.
    if (state.monitors_live) {
        document.getElementById("prop-monitors").innerText = "ONLINE";
        document.getElementById("prop-monitors").className = "indicator-color-green";
    } else {
        document.getElementById("prop-monitors").innerText = "OFFLINE";
        document.getElementById("prop-monitors").className = "indicator-color-red";
    }

    // Channels and designator.
    document.getElementById("prop-channel").innerText = state.channels.toString();
    let channelDesignator = "Unknown";
    let channelDesignatorClass = "indicator-color-red";
    switch (state.channel_designation_status) {
        case "UNLOCKED":
            channelDesignator = "UNLOCKED";
            channelDesignatorClass = "";
            break;
        case "LOCKED":
            channelDesignator = "LOCKED";
            channelDesignatorClass = "indicator-color-green";
            break;
        case "SWEEPING":
            channelDesignator = "SWEEPING";
            channelDesignatorClass = "indicator-color-yellow";
            break;
    }
    document.getElementById("prop-channel-designator").innerText = channelDesignator;
    document.getElementById("prop-channel-designator").className = channelDesignatorClass;

    // Task
    if (state.is_tracking) {
        document.getElementById("prop-task").innerText = "TRACK";
        document.getElementById("prop-task").className = "indicator-color-green";

        document.getElementById("prop-bandit-target").innerText = state.tracking_target;
        document.getElementById("prop-bandit-target").className = "";

        // Track
        if (state.track) {
            document.getElementById("prop-track").innerText = state.track;
            document.getElementById("prop-track").className = "indicator-color-green";

            let frames = state.track_frames;
            if (frames > 1000000) {
                frames = ">1M";
            }
            document.getElementById("prop-frames").innerText = numeral(frames).format("0,0");
            document.getElementById("prop-frames").className = "";

            document.getElementById("prop-contact").innerText = moment(state.track_contact).format('LTS');
            document.getElementById("prop-contact").className = "";

            document.getElementById("prop-bandit-signal").innerText = state.bandit_signal + " dBm";
        } else {
            document.getElementById("prop-track").innerText = "NO CTCT";
            document.getElementById("prop-track").className = "indicator-color-red";

            document.getElementById("prop-frames").innerText = "N/A";
            document.getElementById("prop-frames").className = "indicator-color-inactive";

            document.getElementById("prop-contact").innerText = "N/A";
            document.getElementById("prop-contact").className = "indicator-color-inactive";

            document.getElementById("prop-bandit-signal").innerText = "N/A";
        }
    } else {
        document.getElementById("prop-task").innerText = "NONE";
        document.getElementById("prop-task").className = "indicator-color-inactive";

        document.getElementById("prop-bandit-target").innerText =  "NONE";
        document.getElementById("prop-bandit-target").className = "indicator-color-inactive";

        document.getElementById("prop-track").innerText = "N/A";
        document.getElementById("prop-track").className = "indicator-color-inactive";

        document.getElementById("prop-frames").innerText = "N/A";
        document.getElementById("prop-frames").className = "indicator-color-inactive";

        document.getElementById("prop-contact").innerText = "N/A";
        document.getElementById("prop-contact").className = "indicator-color-inactive";

        document.getElementById("prop-bandit-signal").innerText = "N/A";
    }

    // Bandit signal chart.
    var ctx = document.getElementById('chart-chart');
    new Chart(ctx, {
        type: 'line',
        data: {
            labels: fakeLabels(signalData),
            datasets: [{
                label: 'Bandit Signal',
                backgroundColor: 'rgb(255,60,92)',
                borderColor: 'rgb(255,60,92)',
                borderWidth: 1,
                cubicInterpolationMode: 'monotone',
                steppedLine: true,
                pointRadius: 0,
                data: signalData,
                fill: false,
            }]
        },
        options: {
            title: {display: false},
            legend: {display: false},
            scales:{
                xAxes: [{ display: false }],
                yAxes: [{ display: true, ticks: {min: -100, max: 0, fontFamily: "'Inconsolata', monospace"}}],
            }
        }
    });

    // Events list.
    const events = document.getElementById("events");
    events.innerHTML = "";
    if (state.events && state.events.length > 0) {
        for (let i = state.events.length - 1; i >=0 ; i--) {
            const x = state.events[i];
            let message = moment(x.timestamp).format('LTS') + " [" + x.source + "]: " + x.message;
            const li = document.createElement("li");
            li.appendChild(document.createTextNode(message))
            events.appendChild(li);
        }
    } else {
        const li = document.createElement("li");
        li.appendChild(document.createTextNode("No events."))
        events.appendChild(li);
    }
}