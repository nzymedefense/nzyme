import React, { useEffect, useState } from 'react'
import TapsService from '../../../services/TapsService'
import TapsTable from './TapsTable'

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
        <div>
            <div className="row">
                <div className="col-md-12">
                    <h1>Taps</h1>
                </div>
            </div>

            <div className="row mt-3">
                <div className="col-md-12">
                    <div className="card">
                        <div className="card-body">
                            <h3>All Taps</h3>

                            <TapsTable taps={taps} />
                        </div>
                    </div>
                </div>
            </div>
        </div>
  )
}

export default TapsPage
