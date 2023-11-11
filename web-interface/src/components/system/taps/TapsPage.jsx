import React, { useEffect, useState } from 'react'
import TapsService from '../../../services/TapsService'
import TapsTable from './TapsTable'
import ApiRoutes from "../../../util/ApiRoutes";

const tapsService = new TapsService()

function fetchData(setTaps) {
  tapsService.findAllTaps(setTaps)
}

function TapsPage () {
  const [taps, setTaps] = useState(null)

  useEffect(() => {
    fetchData(setTaps)
    const id = setInterval(() => fetchData(setTaps), 5000)
    return () => clearInterval(id)
  }, [setTaps])

  return (
    <React.Fragment>
      <div className="row">
        <div className="col-md-10">
          <h1>Taps</h1>
        </div>
        <div className="col-md-2">
          <a href={ApiRoutes.SYSTEM.TAPS.PROXY_ADD} className="btn btn-primary float-end">Add Tap</a>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-12">
          <div className="card">
            <div className="card-body">
              <TapsTable taps={taps} />
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )
}

export default TapsPage
