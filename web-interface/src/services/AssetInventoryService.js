import RESTClient from "../util/RESTClient";

class AssetInventoryService {

    findAllDot11Assets() {
        let self = this;
        RESTClient.get("/asset-inventory", {}, function(response) {
            self.setState({ssids: response.data.ssids});
        });
    }

}

export default AssetInventoryService;