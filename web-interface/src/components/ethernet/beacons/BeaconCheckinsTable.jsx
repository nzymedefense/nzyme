import React from "react";
import moment from "moment";
import LoadingSpinner from "../../misc/LoadingSpinner";
import Paginator from "../../misc/Paginator";
import numeral from "numeral";
import AutomaticL4SessionLink from "../shared/AutomaticL4SessionLink";
import L4Address from "../shared/L4Address";
import EthernetMacAddress from "../../shared/context/macs/EthernetMacAddress";
import FilterValueIcon from "../../shared/filtering/FilterValueIcon";

export default function BeaconCheckinsTable({checkins, page, setPage, perPage, filterFields, setFilters}) {

  if (!checkins) {
    return <LoadingSpinner />
  }

  const filterIcon = (field, value) => {
    return <FilterValueIcon setFilters={setFilters} fields={filterFields} field={field} value={value} />
  }

  if (checkins.checkins.length === 0) {
    return <div className="alert alert-info mb-0">No check-ins recorded in the selected time range.</div>
  }

  return (
    <React.Fragment>
      <table className="table table-sm table-hover table-striped">
        <thead>
        <tr>
          <th>Timestamp</th>
          <th>Client Device</th>
          <th>Client MAC</th>
          <th>Destination Address</th>
          <th>Destination MAC</th>
          <th>Size</th>
          <th>Connection</th>
          <th>Tap</th>
        </tr>
        </thead>
        <tbody>
        {checkins.checkins.map((c, i) => {
          return (
            <tr key={i}>
              <td title={moment(c.timestamp).fromNow()}>{moment(c.timestamp).format()}</td>
              <td>
                {c.source ? <L4Address address={c.source} hidePort={true}
                                       filterElement={filterIcon("source_address", c.source_address)} />
                  : <React.Fragment>
                    <span className="machine-data">{c.source_address}</span>{' '}
                    {filterIcon("source_address", c.source_address)}
                  </React.Fragment>}
              </td>
              <td>
                {c.source && c.source.mac ?
                  <EthernetMacAddress addressWithContext={c.source.mac} withAssetLink withAssetName
                                      filterElement={filterIcon("source_mac", c.source_mac)} />
                  : (c.source_mac ?
                    <React.Fragment>
                      <span className="machine-data">{c.source_mac}</span>{' '}{filterIcon("source_mac", c.source_mac)}
                    </React.Fragment>
                    : <span className="text-muted">n/a</span>)}
              </td>
              <td>
                {c.destination ? <L4Address address={c.destination}
                                            filterElement={filterIcon("destination_address", c.destination_address)} />
                  : (c.destination_address ?
                    <React.Fragment>
                      <span className="machine-data">{c.destination_address}</span>{' '}
                      {filterIcon("destination_address", c.destination_address)}
                    </React.Fragment>
                    : <span className="text-muted">n/a</span>)}
              </td>
              <td>
                {c.destination_mac_data ?
                  <EthernetMacAddress addressWithContext={c.destination_mac_data} withAssetLink withAssetName
                                      filterElement={filterIcon("destination_mac", c.destination_mac)} />
                  : (c.destination_mac ?
                    <React.Fragment>
                      <span className="machine-data">{c.destination_mac}</span>{' '}{filterIcon("destination_mac", c.destination_mac)}
                    </React.Fragment>
                    : <span className="text-muted">n/a</span>)}
              </td>
              <td>
                {c.bytes_total !== null && c.bytes_total !== undefined ?
                  <span title={"RX: " + (c.bytes_rx !== null && c.bytes_rx !== undefined ? numeral(c.bytes_rx).format("0,0") + " bytes" : "n/a")
                    + ", TX: " + (c.bytes_tx !== null && c.bytes_tx !== undefined ? numeral(c.bytes_tx).format("0,0") + " bytes" : "n/a")}>
                    {numeral(c.bytes_total).format("0.0b")}
                  </span>
                  : <span className="text-muted">n/a</span>}
              </td>
              <td>
                {c.session_key ?
                  <React.Fragment>
                    <AutomaticL4SessionLink l4Type={c.l4_type} sessionId={c.session_key} startTime={c.timestamp} />
                    {c.session_count > 1 ?
                      <span className="text-muted ms-1" title="Parallel connections collapsed into this check-in">
                        +{c.session_count - 1}
                      </span> : null}
                  </React.Fragment>
                  : <span className="text-muted">n/a</span>}
              </td>
              <td>{c.tap_name ? c.tap_name : <span className="text-muted">n/a</span>}</td>
            </tr>
          )
        })}
        </tbody>
      </table>

      <Paginator itemCount={checkins.total} perPage={perPage} setPage={setPage} page={page} />
    </React.Fragment>
  )

}
