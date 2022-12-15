import RESTClient from "../util/RESTClient";

class CryptoService {

    findAllPGPKeys(setKeys) {
        RESTClient.get('/system/crypto/summary', {}, function (response) {
            setKeys(Object.values(response.data.pgp_keys));
        })
    }

}

export default CryptoService;