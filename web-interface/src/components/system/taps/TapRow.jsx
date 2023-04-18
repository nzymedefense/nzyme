import React from 'react'
import numeral from 'numeral'
import moment from 'moment'
import ApiRoutes from '../../../util/ApiRoutes'
import byteAverageToMbit from '../../../util/Tools'

function TapsRow (props) {

  const tap = props.tap

  if (tap.active) {
    return (
      <tr>
        <td>
          <a href={ApiRoutes.SYSTEM.TAPS.DETAILS(tap.name)}>{tap.name}</a>

          {tap.active
            ? ''
            : <span>&nbsp;
              <i className="fa-solid fa-triangle-exclamation text-danger" title="Tap is offline." />
            </span>}
        </td>
        <td>{byteAverageToMbit(tap.processed_bytes.average)} ({numeral(tap.processed_bytes.average / 10).format('0 b')}/sec)</td>
        <td>{numeral(tap.processed_bytes.total).format('0.0 b')}</td>
        <td>{numeral(tap.cpu_load).format('0.0')}%</td>
        <td>
          {numeral(tap.memory_used).format('0 b')} / {numeral(tap.memory_total).format('0 b')} ({numeral(tap.memory_used / tap.memory_total * 100).format('0.0')}%)
        </td>
        <td>{tap.clock_drift_ms < -5000 || tap.clock_drift_ms > 5000
          ? <i className="fa-solid fa-warning text-danger" title="Clock drift detected"/>
          : <i className="fa-regular fa-circle-check" title="No clock drift detected" />}</td>
        <td>{tap.version}</td>
        <td title={moment(tap.last_report).format()}>
          {moment(tap.last_report).fromNow()}
        </td>
      </tr>
    )
  } else {
    return (
        <tr>
          <td><a href={ApiRoutes.SYSTEM.TAPS.DETAILS(tap.name)}>{tap.name}</a></td>
          <td colSpan={7} style={{textAlign: "center"}} title={moment(tap.last_report).format()}>
            <span><i className="fa-solid fa-triangle-exclamation text-danger" title="Node is offline."/></span>{' '}
            Offline.{' '}
            { tap.last_report ? <span>(Last seen {moment(tap.last_report).fromNow()})</span> : <span>(Never reported in)</span> }
          </td>
        </tr>
    )
  }

}

export default TapsRow
