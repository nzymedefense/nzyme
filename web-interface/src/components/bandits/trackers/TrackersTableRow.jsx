import React, { useCallback } from 'react'

import moment from 'moment'
import { round } from 'lodash'
import { notify } from 'react-notify-toast'
import TrackerStatus from './TrackerStatus'
import TrackingMode from './TrackingMode'
import Routes from '../../../util/ApiRoutes'
import TrackBanditButton from './TrackBanditButton'

function TrackersTableRow (props) {
  const tracker = props.tracker

  const onTrackingStartButtonClicked = useCallback(() => {
    props.trackersService.issueStartTrackingRequest(tracker.name, props.forBandit.uuid, function () {
      notify.show('Issued start tracking request for this bandit to tracker device.', 'success')
      props.setTrackers(undefined)
    })
  }, [tracker, props.trackersService, props.forBandit, props.setTrackers])

  const onTrackingStopButtonClicked = useCallback(() => {
    props.trackersService.issueCancelTrackingRequest(tracker.name, props.forBandit.uuid, function () {
      notify.show('Issued cancel tracking request for this bandit to tracker device.', 'success')
      props.setTrackers(undefined)
    })
  }, [tracker, props.trackersService, props.forBandit, props.setTrackers])

  return (
            <tr>
                <td><a href={Routes.BANDITS.SHOW_TRACKER(tracker.name)}>{tracker.name}</a></td>
                <td><TrackerStatus status={tracker.state} /></td>
                <td>
                    <TrackingMode
                        mode={tracker.tracking_mode}
                        status={tracker.state}
                        bandit={props.forBandit}
                        pendingRequests={tracker.has_pending_tracking_requests} />
                </td>
                <td title={moment(tracker.last_seen).format()}>
                    {moment(tracker.last_seen).fromNow()}
                </td>
                <td>{round(tracker.rssi / 255 * 100)}%</td>
                <td>
                    <TrackBanditButton
                        bandit={props.forBandit}
                        tracker={tracker}
                        onStartTrackingClick={onTrackingStartButtonClicked}
                        onCancelTrackingClick={onTrackingStopButtonClicked}
                    />
                </td>
            </tr>
  )
}

export default TrackersTableRow
