import RESTClient from '../util/RESTClient'

class PluginsService {

    findInitializedPlugins(setPlugins) {
        RESTClient.get('/system/plugins/names', {}, function (response) {
            setPlugins(response.data);
        })
    }

    // TODO change App.js to functional component and remove this
    findInitializedPlugins() {
        const self = this;
        RESTClient.get('/system/plugins/names', {}, function (response) {
            self.setState({plugins: response.data});
        })
    }

}

export default PluginsService;

