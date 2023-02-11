import React, { useCallback } from 'react'
import { notify } from 'react-notify-toast'

import { anyTrackerTrackingBandit } from './../../BanditTools'
import BanditsService from '../../../../services/BanditsService'

const banditsService = new BanditsService()

function DeleteIdentifierButton (props) {

  const bandit = props.bandit
  const trackers = props.tracker
  const identifierUUID = props.identifierUUID
  const setBandit = props.setBandit

  const deleteIdentifier = useCallback((e) => {
    e.preventDefault()

    if (bandit.read_only) {
      alert('Cannot delete identifier of a built-in bandit.')
      return
    }

    if (anyTrackerTrackingBandit(bandit, trackers)) {
      alert('Cannot delete a bandit that is currently tracked by trackers. Please stop tracking first.')
      return
    }

    if (!window.confirm('Delete identifier?')) {
      return
    }

    banditsService.deleteIdentifier(bandit.uuid, identifierUUID, function () {
      notify.show('Identifier deleted.', 'success')
      setBandit(undefined)
    })
  }, [bandit, trackers, identifierUUID, setBandit])
  return (
        <button className="btn btn-sm btn-danger" onClick={deleteIdentifier}>Delete</button>
  )
}

export default DeleteIdentifierButton
