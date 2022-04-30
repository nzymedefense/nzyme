import RESTClient from '../util/RESTClient'
import {notify} from "react-notify-toast";

class TapService {

    findAllTaps(setTaps) {
        RESTClient.get('/taps', {}, function (response) {
            setTaps(response.data);
        })
    }

    findTap(tapName, setTap) {
        RESTClient.get('/taps/show/' + tapName, {}, function (response) {
            setTap(response.data);
        })
    }

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
