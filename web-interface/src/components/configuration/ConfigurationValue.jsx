import React from 'react'

function ConfigurationValue (props) {
  if (props.value) {
    if (Array.isArray(props.awaitingRestart) && props.awaitingRestart.includes(props.configKey)) {
      return (
                <React.Fragment>
                    <i>{props.value}</i>{' '}
                    <strong className="text-danger">(restart required)</strong>
                </React.Fragment>
      )
    } else {
      return <span>{String(props.value)}</span>
    }
  } else {
    if (props.boolean) {
      return <span>false</span>
    } else {
      if (props.required) {
        return (
                    <React.Fragment>
                        <i>(none)</i>{' '}
                        <strong className="text-danger">(required setting)</strong>
                    </React.Fragment>
        )
      } else {
        return <i>(none)</i>
      }
    }
  }
}

export default ConfigurationValue
