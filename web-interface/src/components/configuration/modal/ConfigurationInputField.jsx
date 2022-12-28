import React from 'react'

function ConfigurationInputField (props) {
  function updateValue (value) {
    if (!props.disabled) {
      props.setValue(value)
    }
  }

  switch (props.type) {
    case 'STRING':
      return <input type="text"
                          className="form-control"
                          autoComplete="off"
                          value={props.value ? props.value : ''} onChange={(e) => updateValue(e.target.value) }/>
    case 'STRING_ENCRYPTED':
      return <input type="text"
                          className="form-control"
                          autoComplete="off"
                          value={props.value ? props.value : ''} onChange={(e) => updateValue(e.target.value) }/>
    case 'NUMBER':
      return <input type="number"
                          className="form-control"
                          autoComplete="off"
                          value={props.value ? props.value : ''} onChange={(e) => updateValue(parseInt(e.target.value, 10))} />
    case 'BOOLEAN':
      return (
                <div className="form-check form-switch">
                    <input className="form-check-input"
                           autoComplete="off"
                           type="checkbox"
                           role="switch"
                           id={'switch-' + props.fieldKey}
                           checked={ props.value }
                           onChange={(e) => { updateValue(e.target.checked) } } />
                    <label className="form-check-label" htmlFor={'switch-' + props.fieldKey}>
                        {props.title}
                    </label>
                </div>
      )
    default:
      return <div>Unknown field type.</div>
  }
}

export default ConfigurationInputField; 2
