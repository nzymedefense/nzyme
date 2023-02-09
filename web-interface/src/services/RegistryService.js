import RESTClient from '../util/RESTClient'

class RegistryService {

  deleteKey(key, successCallback) {
    RESTClient.delete('/system/registry/show/' + key, successCallback);
  }

}

export default RegistryService;