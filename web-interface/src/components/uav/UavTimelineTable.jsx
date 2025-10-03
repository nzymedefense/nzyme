import React from "react";
import LoadingSpinner from "../misc/LoadingSpinner";
import moment from "moment";
import Paginator from "../misc/Paginator";
import UavActiveIndicator from "./util/UavActiveIndicator";
import numeral from "numeral";
import LongDistance from "../shared/LongDistance";

export default function UavTimelineTable(props) {

  const timeline = props.timeline;
  const onPlotTrack = props.onPlotTrack;
  const plottedTrackLoading = props.plottedTrackLoading;

  const page = props.page;
  const perPage = props.perPage;
  const setPage = props.setPage;

  if (timeline == null) {
    return <LoadingSpinner />
  }

  if (timeline.count === 0) {
    return <div className="alert alert-info mb-0">No UAV tracks recorded.</div>
  }

  return (
    <React.Fragment>
      <p className="text-muted">
        Timeline &amp; Tracks always use data from all taps of your selected tenants and override a possible manual
        tap selection.
      </p>

      <table className="table table-sm table-hover table-striped">
        <thead>
        <tr>
          <th style={{width: 25}}>&nbsp;</th>
          <th style={{width: 70}}>Track ID</th>
          <th style={{width: 95}}>&nbsp;</th>
          <th style={{width: 130}} title="Minimum distance from any selected tap">Minimum Distance</th>
          <th style={{width: 130}} title="Maximum distance from any selected tap">Maximum Distance</th>
          <th>From</th>
          <th>To</th>
          <th>Duration</th>
        </tr>
        </thead>
        <tbody>
        {timeline.timeline.map((t, i) => {
          return (
            <tr key={i}>
              <td><UavActiveIndicator active={t.is_active} /></td>
              <td>{t.uuid.substring(0, 7)}</td>
              <td>
                { plottedTrackLoading && plottedTrackLoading === t.uuid
                    ? <LoadingSpinner />
                    : <a href="#" onClick={(e) => onPlotTrack(e, t.uuid)}>Paint on Map</a>}
              </td>
              <td><LongDistance feet={t.min_distance} /></td>
              <td><LongDistance feet={t.max_distance} /></td>
              <td>{moment(t.seen_from).format()} <span className="text-muted">({moment(t.seen_from).fromNow()})</span></td>
              <td>{moment(t.seen_to).format()} <span className="text-muted">({moment(t.seen_to).fromNow()})</span></td>
              <td title={numeral(t.duration_seconds).format("0,0") + " sec"}>
                {t.duration_human_readable}
              </td>
            </tr>
          )
        })}
        </tbody>
      </table>

      <Paginator itemCount={timeline.count} perPage={perPage} setPage={setPage} page={page} />
    </React.Fragment>
  )

}