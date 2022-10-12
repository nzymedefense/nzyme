import RESTClient from '../util/RESTClient'

class PluginsService {

    findInitializedPlugins(setPlugins) {
        RESTClient.get('/system/plugins/names', {}, function (response) {
            setPlugins(response.data);
        })
    }

}

export default PluginsService;

