import React from 'react'
import { startCase } from 'lodash/string'

function IdentifierTypeSelector (props) {
  return (
          <select ref={props.selector} onChange={props.onChange}>
              <option key="default-empty" />
              {Object.keys(props.types).map(function (key, i) {
                return <option value={props.types[key]} key={props.types[key]}>{startCase(props.types[key])}</option>
              })}
          </select>
  )
}

export default IdentifierTypeSelector
