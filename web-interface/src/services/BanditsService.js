import RESTClient from '../util/RESTClient'
import { notify } from 'react-notify-toast'

class BanditsService {
  findAll (setBandits) {
    RESTClient.get('/bandits', {}, function (response) {
      setBandits(response.data.bandits);
    })
  }

  findOne (id, setBandits) {
    RESTClient.get('/bandits/show/' + id, {}, function (response) {
      setBandits(response.data);
    })
  }

  findContactOfBandit (banditUUID, contactUUID, detailedSSIDs, detailedBSSIDs, setContact) {
    RESTClient.get('/bandits/show/' + banditUUID + '/contacts/' + contactUUID, { detailed_ssids: detailedSSIDs, detailed_bssids: detailedBSSIDs }, function (response) {
      setContact(response.data);
    })
  }

  createBandit (name, description, successCallback, errorCallback) {
    RESTClient.post('/bandits', { name: name, description: description }, function () {
      successCallback()
    }, function () {
      errorCallback()
    })
  }

  updateBandit (id, name, description, successCallback, errorCallback) {
    RESTClient.put('/bandits/show/' + id, { name: name, description: description }, function () {
      successCallback()
    }, function () {
      errorCallback()
    })
  }

  deleteBandit (banditUUID, setDeleted) {
    RESTClient.delete('/bandits/show/' + banditUUID, function () {
      setDeleted(true);
    }, function () {
      notify.show('Could not delete bandit. Please check nzyme log file.', 'error')
    })
  }

  findAllIdentifierTypes (setBanditIdentifierTypes) {
  
    RESTClient.get('/bandits/identifiers/types', {}, function (response) {
      setBanditIdentifierTypes(response.data.types);
    })
  }

  createIdentifier (banditUUID, createRequest, successCallback, errorCallback) {
    const self = this

    RESTClient.post('/bandits/show/' + banditUUID + '/identifiers', createRequest, successCallback, errorCallback);
  }

  deleteIdentifier (banditUUID, identifierUUID, successCallback) {
    RESTClient.delete('/bandits/show/' + banditUUID + '/identifiers/' + identifierUUID, successCallback, function () {
      notify.show('Could not delete identifier. Please check nzyme log file.', 'error');
    })
  }
}

export default BanditsService
