import React from 'react'
import numeral from 'numeral'

function FrameCount (props) {
  if (props.alert.frame_count) {
    return (
            <span>{numeral(props.alert.frame_count).format('0,0')}</span>
    )
  } else {
    return (
            <span>n/a</span>
    )
  }
}

export default FrameCount
