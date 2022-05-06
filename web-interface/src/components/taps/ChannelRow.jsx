import React from "react";
import numeral from "numeral";

function ChannelRow(props) {

    const c = props.channel;

    return (
        <tr>
            <td>{c.name}</td>
            <td>{numeral(c.watermark).format()} / {numeral(c.capacity).format()}</td>
            <td>
                {numeral(c.throughput_messages.average/10).format("0,0")} messages/sec
            </td>
            <td>
                {numeral(c.throughput_bytes.average/10).format("0,0b")}/sec
            </td>
            <td>
                {numeral(c.errors.average/10).format("0,0")} errors/sec
                ({numeral(c.errors.total).format()} since last restart)
            </td>
        </tr>
    )

}

export default ChannelRow;