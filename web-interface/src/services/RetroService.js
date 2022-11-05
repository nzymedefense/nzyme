import RESTClient from '../util/RESTClient'

class RetroService {

    getServiceSummary(setSummary) {
        RESTClient.get('/retro/service/summary', {}, function (response) {
            setSummary(response.data);
        })
    }

}

export default RetroService;
