import React from 'react'

function ConfigurationValueHelp (props) {
  return (
        <span className="float-end">
            {props.helpTag
              ? <a href={'https://go.nzyme.org/' + props.helpTag} className="configuration-help" target="_blank" rel="noreferrer">
                    Help
                </a>
              : null }
        </span>
  )
}

export default ConfigurationValueHelp
