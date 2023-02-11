import React, { useState, useEffect } from 'react'

import LoadingSpinner from '../../misc/LoadingSpinner'
import TrackersTableRow from './TrackersTableRow'
import GroundStationDisabled from './GroundStationDisabled'
import TrackersService from '../../../services/TrackersService'

const trackersService = new TrackersService()

function fetchData (setTrackers, setGroundstationEnabled) {
  trackersService.findAll(setTrackers, setGroundstationEnabled)
}

function TrackersTable (props) {
  const [trackers, setTrackers] = useState()
  const [groundstationEnabled, setGroundstationEnabled] = useState()

  useEffect(() => {
    fetchData(setTrackers, setGroundstationEnabled)
    const id = setInterval(() => fetchData(setTrackers, setGroundstationEnabled), 5000)
    return () => clearInterval(id)
  }, [trackers])

  if (!trackers) {
    return <LoadingSpinner />
  }

  if (!groundstationEnabled) {
    return <GroundStationDisabled />
  }

  if (trackers.length === 0) {
    return (
                <div className="alert alert-info">
                    No tracker devices have connected yet.
                </div>
    )
  }

  return (
            <div className="row">
                <div className="col-md-12">
                    <table className="table table-sm table-hover table-striped">
                        <thead>
                        <tr>
                            <th>Name</th>
                            <th>Status</th>
                            <th>Tracking Mode</th>
                            <th>Last Ping</th>
                            <th>Signal Strength</th>
                            <th>&nbsp;</th>
                        </tr>
                        </thead>
                        <tbody>
                        {Object.keys(trackers).map(function (key, i) {
                          return <TrackersTableRow
                                key={'tracker-' + key}
                                tracker={trackers[key]}
                                forBandit={props.forBandit}
                                setTrackers={setTrackers}
                            />
                        })}
                        </tbody>
                    </table>
                </div>
            </div>
  )
}

export default TrackersTable
