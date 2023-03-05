import React from 'react'

function LoadingSpinner(props) {

  if (props.show !== undefined && props.show === false) {
    return null;
  }

  return (
    <div className="row">
      <div className="col-md-12">
        loading...
      </div>
    </div>
  )

}

export default LoadingSpinner
