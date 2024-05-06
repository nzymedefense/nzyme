import React from 'react'

import numeral from 'numeral'

function NumberCard (props) {

  const title = props.title;
  const value = props.value;
  const numberFormat = props.numberFormat;

  return (
        <div className="card">
            <div className="card-body card-number">
                <div className="card-number-title">
                    <span className="title">{title}</span>
                </div>

                <span className="value">
                    {numeral(value).format(numberFormat)}
                </span>
            </div>
        </div>
  )
}

export default NumberCard
