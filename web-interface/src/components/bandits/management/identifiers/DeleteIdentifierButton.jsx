import React, { useCallback } from 'react'
import { notify } from 'react-notify-toast'

import { anyTrackerTrackingBandit } from './../../BanditTools'
import BanditsService from '../../../../services/BanditsService'

const banditsService = new BanditsService()

function DeleteIdentifierButton (props) {
  const deleteIdentifier = useCallback((e) => {
    e.preventDefault()

    if (props.bandit.read_only) {
      alert('Cannot delete identifier of a built-in bandit.')
      return
    }

    if (anyTrackerTrackingBandit(props.bandit, props.trackers)) {
      alert('Cannot delete a bandit that is currently tracked by trackers. Please stop tracking first.')
      return
    }

    if (!window.confirm('Delete identifier?')) {
      return
    }

    banditsService.deleteIdentifier(props.bandit.uuid, props.identifier.uuid, function () {
      notify.show('Identifier deleted.', 'success')
      props.setBandit(undefined)
    })
  }, [props.bandit, props.trackers, banditsService, props.identifierUUID, props.setBandit])
  return (
        <button className="btn btn-sm btn-danger" onClick={deleteIdentifier}>Delete</button>
  )
}

export default DeleteIdentifierButton
