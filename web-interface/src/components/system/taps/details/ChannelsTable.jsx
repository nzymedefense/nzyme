import React from 'react'
import ChannelRow from './ChannelRow'

function ChannelsTable (props) {
  const tap = props.tap;
  const busName = props.busName;

  return (
        <table className="table table-sm table-hover table-striped">
            <thead>
            <tr>
                <th>Channel</th>
                <th>Buffer Usage</th>
                <th>Throughput (messages)</th>
                <th>Throughput (data size)</th>
                <th>Errors</th>
            </tr>
            </thead>
            <tbody>
            {Object.keys(props.channels.sort((a, b) => a.name.localeCompare(b.name))).map(function (key, i) {
              return <ChannelRow key={'channel-' + i} channel={props.channels[i]} tap={tap} busName={busName} />
            })}
            </tbody>
        </table>
  )
}

export default ChannelsTable
