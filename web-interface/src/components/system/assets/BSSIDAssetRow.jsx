import React from 'react'
import { join } from 'lodash/array'

class BSSIDAssetRow extends React.Component {
  render () {
    return (
            <tr>
                <td>{this.props.bssid.address}</td>
                <td>{join(this.props.bssid.fingerprints, ', ')}</td>
            </tr>
    )
  }
}

export default BSSIDAssetRow
