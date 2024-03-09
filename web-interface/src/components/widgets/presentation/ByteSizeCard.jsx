import React from 'react'

import numeral from 'numeral'

function ByteSizeCard (props) {
  return (
        <div className="card">
            <div className="card-body card-number">
                <div className="card-number-title">
                    <span className="title">{props.title}</span>
                </div>

                <span className="value">
                    {numeral(props.value).format('0.00b')}
                </span>
            </div>
        </div>
  )
}

export default ByteSizeCard
