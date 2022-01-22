import React from 'react'

// This is almost entirely duplicated from BSSIDTable because they might be handled differently in the future.

class AdvertisedSSIDTable extends React.Component {
  constructor (props) {
    super(props)

    this.state = {
      selection: []
    }

    this._updateSelection = this._updateSelection.bind(this)
  }

  _updateSelection (x) {
    const current = this.state.selection
    let selection

    if (this.state.selection.includes(x)) {
      selection = current.filter(item => item !== x)
    } else {
      current.push(x)
      selection = current
    }

    this.setState({ selection: selection })
    this.props.onNewSelection(selection)
  }

  render () {
    const ssids = this.props.ssids
    const self = this

    if (!ssids || ssids.length === 0) {
      return <div className="alert alert-info">No SSIDS recorded.</div>
    }

    return (
            <table className="table table-sm table-hover table-striped">
                <thead>
                <tr>
                    <th>SSID</th>
                    <th>Frames</th>
                    <th>Chart</th>
                </tr>
                </thead>
                <tbody>
                {Object.keys(ssids).map(function (key, i) {
                  return (
                        <tr key={'bssid-' + i}>
                            <td>{ssids[key].value}</td>
                            <td>{ssids[key].frame_count}</td>
                            <td>
                                <input type="checkbox" onClick={() => self._updateSelection(ssids[key].value)} />
                            </td>
                        </tr>
                  )
                })}
                </tbody>
            </table>
    )
  }
}

export default AdvertisedSSIDTable
