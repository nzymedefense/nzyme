import React, {useState} from 'react'

function AssetImage(props) {

  const filename = props.filename;
  const className = props.className;
  const alt = props.alt;
  const id = props.id;
  const filenameHover = props.filenameHover;

  const path = (filename) => {
    return window.appConfig.assetsUri + 'static/' + filename;
  }

  const [src, setSrc] = useState(path(filename));

  return (
        <img src={src}
             onMouseOver={() => setSrc(path(filenameHover))}
             onMouseOut={() => setSrc(path(filename))}
             className={className}
             alt={alt}
             id={id} />
  )
}

export default AssetImage
