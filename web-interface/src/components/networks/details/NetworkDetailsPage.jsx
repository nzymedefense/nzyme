import React from 'react'
import { useParams } from 'react-router-dom'

import NetworkDetails from './NetworkDetails'
import Routes from '../../../util/ApiRoutes'

function NetworkDetailsPage () {
  const { bssid, ssid, channel } = useParams()
  return (
        <div>
            <div className="row">
                <div className="col-md-12">
                    <nav aria-label="breadcrumb">
                        <ol className="breadcrumb">
                            <li className="breadcrumb-item"><a href={Routes.NETWORKS.INDEX}>Networks</a></li>
                            <li className="breadcrumb-item active" aria-current="page">{bssid} {ssid} (Channel {channel})</li>
                        </ol>
                    </nav>
                </div>
            </div>

            <div className="row">
                <div className="col-md-12">
                    <h1>Network Details</h1>
                </div>
            </div>

            <NetworkDetails bssid={bssid} ssid={ssid} channelNumber={channel} />
        </div>
  )
}

export default NetworkDetailsPage
