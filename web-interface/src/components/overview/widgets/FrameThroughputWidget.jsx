import React from 'react'
import SimpleLineChart from '../../charts/SimpleLineChart'

class FrameThroughputWidget extends React.Component {
  render () {
    return (
            <SimpleLineChart
                width={1100}
                height={150}
                customMarginRight={0}
                data={this.props.frameThroughputHistogram}/>
    )
  }
}

export default FrameThroughputWidget
