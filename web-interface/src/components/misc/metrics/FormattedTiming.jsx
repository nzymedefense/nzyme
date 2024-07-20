import React from 'react'
import numeral from 'numeral'

class FormattedTiming extends React.Component {
  render () {
    return (
      <span title={this.props.timing}>
        {numeral(this.props.timing / 1000).format(this.props.format)} ms
      </span>
    )
  }
}

export default FormattedTiming
