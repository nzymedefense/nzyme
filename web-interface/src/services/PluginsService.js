import RESTClient from '../util/RESTClient'
import Store from "../util/Store";

class PluginsService {

    findInitializedPlugins(setPlugins) {
        RESTClient.get('/system/plugins/names', {}, function (response) {
            setPlugins(response.data);
        })
    }

    loadInitializedPluginsIntoStore() {
        RESTClient.get('/system/plugins/names', {}, function (response) {
            Store.set("plugins", response.data);
        })
    }

}

export default PluginsService;

