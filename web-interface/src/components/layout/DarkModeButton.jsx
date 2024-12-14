import React from 'react'
import Store from '../../util/Store'

function DarkModeButton (props) {
  if (props.darkModeEnabled) {
    return (
            <button className="btn btn-outline-secondary" onClick={() => { props.setDarkModeEnabled(false) }} title="Enable Light Mode">
                <i className="fa-solid fa-sun" />
            </button>
    )
  } else {
    return (
            <button className="btn btn-outline-secondary" onClick={() => { props.setDarkModeEnabled(true) }} title="Enable Dark Mode">
                <i className="fa-solid fa-moon" />
            </button>
    )
  }
}

export default DarkModeButton
