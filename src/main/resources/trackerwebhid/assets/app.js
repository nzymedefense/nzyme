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

function render(state) {
    document.getElementById("prop-clock").innerText = moment(state.clock).format('LTS');
}