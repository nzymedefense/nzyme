setInterval(function(){
    axios.get("http://10.0.0.1:13000/api/state")
        .then(function (response) {
            render(response.data)
        })
        .catch(function (error) {
            // TODO notify show
            console.log("failed");
            console.log(error);
        });
}, 1000);

// TODO: notification in case of fetch error
//       loading screen when not connected

function render(state) {
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
        document.getElementById("prop-signal").innerText = "n/a";
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

}