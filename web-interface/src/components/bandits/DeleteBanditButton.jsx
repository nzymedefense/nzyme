import React, { useCallback } from 'react'
import { anyTrackerTrackingBandit } from './BanditTools';

function DeleteBanditButton(props) {

    const deleteBandit = useCallback(() => {
        if (props.bandit.read_only) {
            alert('Cannot delete a built-in bandit.')
            return;
          }
        
          if (anyTrackerTrackingBandit(props.bandit, props.trackers)) {
            alert('Cannot delete a bandit that is currently tracked by trackers. Please stop tracking first.')
            return;
          }
        
          if (!window.confirm('Delete bandit?')) {
            return;
          }

          props.banditsService.deleteBandit(props.bandit.uuid, props.setDeleted);
    }, [props.bandit, props.trackers, props.banditsService, props.setDeleted]);

    return (
        <button className="btn btn-danger" onClick={deleteBandit}>Delete Bandit</button>
    )

}

export default DeleteBanditButton;