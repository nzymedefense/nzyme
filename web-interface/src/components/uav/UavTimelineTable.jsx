import React from "react";
import LoadingSpinner from "../misc/LoadingSpinner";
import moment from "moment";
import Paginator from "../misc/Paginator";
import UavActiveIndicator from "./util/UavActiveIndicator";

export default function UavTimelineTable(props) {

  const timeline = props.timeline;
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
      <table className="table table-sm table-hover table-striped">
        <thead>
        <tr>
          <th style={{width: 25}}>&nbsp;</th>
          <th style={{width: 70}}>Track ID</th>
          <th style={{width: 95}}>&nbsp;</th>
          <th>From</th>
          <th>To</th>
        </tr>
        </thead>
        <tbody>
        {timeline.timeline.map((t, i) => {
          return (
            <tr key={i}>
              <td><UavActiveIndicator active={t.is_active} /></td>
              <td>{t.uuid.substring(0, 7)}</td>
              <td><a href="#">Paint on Map</a></td>
              <td>{moment(t.seen_from).format()} <span className="text-muted">({moment(t.seen_from).fromNow()})</span></td>
              <td>{moment(t.seen_to).format()} <span className="text-muted">({moment(t.seen_to).fromNow()})</span></td>
            </tr>
          )
        })}
        </tbody>
      </table>

      <Paginator itemCount={timeline.count} perPage={perPage} setPage={setPage} page={page} />
    </React.Fragment>
  )

}