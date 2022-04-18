import RESTClient from '../util/RESTClient'
import {notify} from "react-notify-toast";

class TapService {

    getTapSecret (setTapSecret) {
        RESTClient.get('/taps/secret', {}, function (response) {
            setTapSecret(response.data.secret);
        })
    }

    cycleTapSecret(setTapSecret) {
        RESTClient.post('/taps/secret/cycle', {}, function (response) {
            setTapSecret(response.data.secret);
            notify.show("Tap secret has been cycled. You must now update it in the configuration of all your taps.", "success");
        })
    }

}

export default TapService;
