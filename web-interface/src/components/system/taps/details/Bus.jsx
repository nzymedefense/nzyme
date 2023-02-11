import React from 'react'
import ChannelsTable from './ChannelsTable'

function Bus (props) {
  return (
        <div>
            <h6>Bus: {props.bus.name}</h6>

            <ChannelsTable channels={props.bus.channels} />
        </div>
  )
}

export default Bus
