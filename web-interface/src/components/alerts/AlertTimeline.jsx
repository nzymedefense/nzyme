import React, {useEffect, useState} from "react";
import DetectionAlertsService from "../../services/DetectionAlertsService";
import LoadingSpinner from "../misc/LoadingSpinner";
import Paginator from "../misc/Paginator";

import moment from "moment";

const detectionAlertService = new DetectionAlertsService();

function AlertTimeline(props) {

  const alertUUID = props.alertUUID;

  const [timeline, setTimeline] = useState(null);

  const perPage = 5;
  const [page, setPage] = useState(1);

  useEffect(() => {
    setTimeline(null);
    detectionAlertService.findAlertTimeline(alertUUID, setTimeline, perPage, (page-1)*perPage)
  }, [page]);

  if (!timeline) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
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
                  <td title={entry.duration_seconds + " sec"}>
                    {entry.duration_human_readable}
                  </td>
                </tr>
            )
          })}
          </tbody>
        </table>

        <Paginator itemCount={timeline.total} perPage={perPage} setPage={setPage} page={page} />
      </React.Fragment>
  )

}

export default AlertTimeline;