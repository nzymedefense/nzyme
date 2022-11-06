import RESTClient from '../util/RESTClient'

class RetroService {

    getServiceSummary(setSummary) {
        RESTClient.get('/retro/service/summary', {}, function (response) {
            setSummary(response.data);
        })
    }

    getConfiguration(setConfiguration) {
        RESTClient.get('/retro/configuration', {}, function (response) {
            setConfiguration(response.data);
        })
    }

}

export default RetroService;
