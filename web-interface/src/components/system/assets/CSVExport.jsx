import React from 'react'
import LoadingSpinner from '../../misc/LoadingSpinner'
import CSVExportBox from './CSVExportBox'

class CSVExport extends React.Component {
  render () {
    if (!this.props.ssids || !this.props.bssids) {
      return <LoadingSpinner />
    } else {
      return (
                <div className="assets-csv-export">
                    <hr />

                    <CSVExportBox content={this.props.ssids} title="SSIDs CSV" />
                    <CSVExportBox content={this.props.bssids} title="BSSIDs CSV" />

                    <hr />
                </div>
      )
    }
  }
}

export default CSVExport
