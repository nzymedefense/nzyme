import React, {useEffect, useState} from "react";
import LoadingSpinner from "../misc/LoadingSpinner";
import MonitorsService from "../../services/MonitorsService";
import Paginator from "../misc/Paginator";
import moment from "moment/moment";
import numeral from "numeral";

const monitorsService = new MonitorsService();

export default function MonitorDetectionsTimelineTable({monitor}) {

  const [timeline, setTimeline] = useState(null);

  const perPage = 25;
  const [page, setPage] = useState(1);

  useEffect(() => {
    monitorsService.findDetectionsTimelineOfMonitor(monitor.uuid, perPage, (page-1)*perPage, setTimeline)
  }, [monitor, page, perPage]);

  if (!timeline) {
    return <LoadingSpinner />
  }

  if (timeline.entries.length === 0) {
    return <div className="alert alert-info mb-0">This monitor has never triggered a detection alert.</div>
  }

  return (
    <>
      <table className="table table-sm table-hover table-striped">
        <thead>
        <tr>
          <th>Seen From</th>
          <th>Seen To</th>
          <th>Duration</th>
        </tr>
        </thead>
        <tbody>
        {timeline.entries.map(function(entry, i) {
          return (
            <tr key={"timeline-" + i}>
              <td title={moment(entry.seen_from).fromNow()}>
                {moment(entry.seen_from).format()}
              </td>
              <td title={moment(entry.seen_to).fromNow()}>
                {moment(entry.seen_to).format()}
              </td>
              <td title={numeral(entry.duration_seconds).format("0,0") + " sec"}>
                {entry.duration_human_readable}
              </td>
            </tr>
          )
        })}
        </tbody>
      </table>

      <Paginator itemCount={timeline.total} perPage={perPage} setPage={setPage} page={page} />
    </>
  )

}