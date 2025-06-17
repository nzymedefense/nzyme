import RESTClient from '../util/RESTClient'

class UserProfileService {

  findOwnProfile(setProfile) {
    RESTClient.get('/user/profile', {}, function (response) {
      setProfile(response.data);
    })
  }

  changeOwnPassword(currentPassword, newPassword, successCallback, errorCallback) {
    RESTClient.put('/user/password',
        {current_password: currentPassword, new_password: newPassword}, successCallback, errorCallback)
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
