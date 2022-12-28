import RESTClient from '../util/RESTClient'
import { notify } from 'react-notify-toast'

class TrackersService {
  findAll (setTrackers, setGroundstationEnabled) {
    RESTClient.get('/trackers', {}, function (response) {
      setTrackers(response.data.trackers)
      setGroundstationEnabled(response.data.groundstation_enabled)
    })
  }

  findOne (trackerName, setTracker) {
    RESTClient.get('/trackers/show/' + trackerName, {}, function (response) {
      setTracker(response.data)
    })
  }

  issueStartTrackingRequest (trackerName, banditUUID, successCallback) {
    RESTClient.post('/trackers/show/' + trackerName + '/command/start_track_request', { bandit_uuid: banditUUID }, successCallback, function () {
      notify.show('Could not issue cancel tracking request. Please check nzyme log file.', 'error')
    })
  }

  issueCancelTrackingRequest (trackerName, banditUUID, successCallback) {
    RESTClient.post('/trackers/show/' + trackerName + '/command/cancel_track_request', { bandit_uuid: banditUUID }, successCallback, function () {
      notify.show('Could not issue cancel tracking request. Please check nzyme log file.', 'error')
    })
  }
}

export default TrackersService
