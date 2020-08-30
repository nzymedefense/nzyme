import RESTClient from "../util/RESTClient";

class DashboardService {

    findAll() {
        let self = this;

        RESTClient.get("/dashboard", {}, function(response) {
            self.setState({dashboard: response.data});
        });
    }

}

export default DashboardService;