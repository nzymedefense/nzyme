import React from 'react'

import numeral from "numeral";

function HistogramRow(props) {

    return (
        <tr>
            <td>{props.title}</td>
            <td>{numeral(props.histogram.max).format("0b")}</td>
            <td>{numeral(props.histogram.min).format("0b")}</td>
            <td>{numeral(props.histogram.mean).format("0b")}</td>
            <td>{numeral(props.histogram.percentile_95).format("0b")}</td>
            <td>{numeral(props.histogram.percentile_99).format("0b")}</td>
            <td>{numeral(props.histogram.percentile_999).format("0b")}</td>
        </tr>
    )

}

export default HistogramRow;