import React from 'react'

class RSSI extends React.Component {
  render () {
    const rssi = this.props.rssi

    let color
    if (rssi >= -60) {
      color = 'text-success'
    } else if (rssi >= -70) {
      color = 'text-warning'
    } else {
      color = 'text-danger'
    }

    return (
            <span className={color}>{rssi} dBm</span>
    )
  }
}

export default RSSI
