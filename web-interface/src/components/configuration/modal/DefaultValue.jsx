import React from 'react'

function DefaultValue (props) {
  if (props.value) {
    return <span>Default value: {props.value}</span>
  } else {
    return <span>(No default value)</span>
  }
}

export default DefaultValue
