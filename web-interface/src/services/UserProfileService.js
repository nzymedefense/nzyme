import RESTClient from '../util/RESTClient'

class UserProfileService {

  findOwnProfile(setProfile) {
    RESTClient.get('/user/profile', {}, function (response) {
      setProfile(response.data);
    })
  }

  findOwnMfaRecoveryCodes(setCodes) {
    RESTClient.get('/user/mfa/recoverycodes', {}, function (response) {
      setCodes(response.data);
    })
  }

  resetOwnMfa(successCallback) {
    RESTClient.put('/user/mfa/reset', {}, successCallback)
  }

}

export default UserProfileService
