import RESTClient from '../util/RESTClient'
import {notify} from "react-notify-toast";

class RetroService {

    getServiceSummary(setSummary) {
        RESTClient.get('/retro/service/summary', {}, function (response) {
            setSummary(response.data);
        })
    }

}

export default RetroService;
