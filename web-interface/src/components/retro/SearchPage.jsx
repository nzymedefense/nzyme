import React from 'react'
import RetroNotReadyAlert from './RetroNotReadyAlert'

function SearchPage () {
  return (
        <div>
            <RetroNotReadyAlert />

            <div className="row">
                <div className="col-md-12">
                    <h1>Retrospective</h1>
                </div>
            </div>

            <div className="row mt-3">
                <div className="col-md-6">
                    <div className="card">
                        <div className="card-body">
                            <h3>Test</h3>
                        </div>
                    </div>
                </div>
            </div>

        </div>
  )
}

export default SearchPage
