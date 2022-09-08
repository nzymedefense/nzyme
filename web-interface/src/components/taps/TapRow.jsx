import React from "react";
import numeral from 'numeral'
import moment from "moment";
import ApiRoutes from "../../util/ApiRoutes";
import byteAverageToMbit from "../../util/Tools";

function TapsRow(props) {

    const tap = props.tap;

    if (!tap.active) {
        return (
            <tr>
                <td>
                    <a href={ApiRoutes.SYSTEM.TAPS.DETAILS(tap.name)}>{tap.name}</a>
                    <span>&nbsp;
                        <i className="fa-solid fa-triangle-exclamation text-danger" title="Tap is offline." />
                    </span>
                </td>
                <td>-</td>
                <td>-</td>
                <td>-</td>
                <td>-</td>
                <td title={moment(tap.updated_at).format()}>
                    {moment(tap.updated_at).fromNow()}
                </td>
            </tr>
        )
    }

    return (
        <tr>
            <td>
                <a href={ApiRoutes.SYSTEM.TAPS.DETAILS(tap.name)}>{tap.name}</a>

                {tap.active ? "" :
                    <span>&nbsp;
                        <i className="fa-solid fa-triangle-exclamation text-danger" title="Tap is offline." />
                    </span>}
            </td>
            <td>{byteAverageToMbit(tap.processed_bytes.average)} ({numeral(tap.processed_bytes.average/10).format('0 b')}/sec)</td>
            <td>{numeral(tap.processed_bytes.total).format('0.0 b')}</td>
            <td>{numeral(tap.cpu_load).format('0.0')}%</td>
            <td>
                {numeral(tap.memory_used).format('0 b')} / {numeral(tap.memory_total).format('0 b')} ({numeral(tap.memory_used/tap.memory_total*100).format('0.0')}%)
            </td>
            <td title={moment(tap.updated_at).format()}>
                {moment(tap.updated_at).fromNow()}
            </td>
        </tr>
    )

}

export default TapsRow;