import React from 'react'
import AssetStylesheet from '../misc/AssetStylesheet'

function DarkMode (props) {
  if (props.enabled) {
    return (
            <AssetStylesheet filename="dark.css" />
    )
  } else {
    return null
  }
}

export default DarkMode
