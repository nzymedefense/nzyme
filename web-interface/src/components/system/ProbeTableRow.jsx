import React from 'react'
import numeral from 'numeral'

class ProbesTableRow extends React.Component {
  static _decideStatus (probe) {
    if (probe.is_in_loop) {
      if (probe.is_active) {
        return (
                    <i className="fas fa-check-square text-success"/>
        )
      } else {
        return (
                    <i className="fas fa-exclamation-triangle text-danger" title="NOT ACTIVE! Check nzyme logs." />
        )
      }
    } else {
      return (
                <i className="fas fa-exclamation-triangle text-danger" title="NOT RUNNING! Check nzyme logs." />
      )
    }
  }

  // Limit to 13 total channels and abbreviate if it's more.
  static _printChannels (channels) {
    if (channels.length > 13) {
      return channels.slice(0, 13).toString() + ' ...'
    } else {
      return channels.toString()
    }
  }

  render () {
    const probe = this.props.probe

    return (
            <tr>
                <td>{probe.name}</td>
                <td>{ProbesTableRow._decideStatus(probe)}</td>
                <td>{probe.class_name}</td>
                <td>{probe.network_interface}</td>
                <td title={probe.channels.toString()}>{ProbesTableRow._printChannels(probe.channels)}</td>
                <td>{numeral(probe.total_frames).format('0,0')}</td>
            </tr>
    )
  }
}

export default ProbesTableRow
