import React from 'react'

function AssetImage(props) {
  return (
        <img src={window.appConfig.assetsUri + 'static/' + props.filename}
             className={props.className}
             alt={props.alt}
             id={props.id} />
  )
}

export default AssetImage
