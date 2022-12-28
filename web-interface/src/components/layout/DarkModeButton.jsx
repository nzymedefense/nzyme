import React from 'react'
import Store from '../../util/Store'

function DarkModeButton (props) {
  if (Store.get('dark_mode')) {
    return (
            <button className="btn btn-outline-dark" onClick={() => { props.setDarkMode(false) }} title="Enable Light Mode">
                <i className="fa-solid fa-sun" />
            </button>
    )
  } else {
    return (
            <button className="btn btn-outline-dark" onClick={() => { props.setDarkMode(true) }} title="Enable Dark Mode">
                <i className="fa-solid fa-moon" />
            </button>
    )
  }
}

export default DarkModeButton
