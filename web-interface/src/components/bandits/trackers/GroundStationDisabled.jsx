import React from 'react'

class GroundStationDisabled extends React.Component {
  render () {
    return (
            <div className="alert alert-warning">
                <p>No tracker device is configured on your nzyme leader instance and that is why no trackers can register and
                show up here.</p>

                <p>Trackers are devices you can use to physically locate bandits. Read more about this feature in
                    the <a href="https://go.nzyme.org/bandits-and-trackers" target="_blank" rel="noopener noreferrer">documentation</a>.
                </p>
            </div>
    )
  }
}

export default GroundStationDisabled
