import React, {useEffect, useState} from "react";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import TapsService from "../../../../services/TapsService";
import Paginator from "../../../misc/Paginator";
import numeral from "numeral";
import moment from "moment";

const tapsService = new TapsService();

export default function TapEngagementLogsTable({tapUuid}) {

  const [logs, setLogs] = useState(null);

  const [page, setPage] = useState(1);
  const perPage = 25;

  useEffect(() => {
    tapsService.findEngagementLogOfTap(tapUuid, perPage, (page-1)*perPage, setLogs)
  }, [tapUuid, logs, page, perPage])

  if (logs === null) {
    return <LoadingSpinner />
  }

  if (logs.logs.length === 0) {
    return <div className="alert alert-info mb-0">No engagement interface logs recorded.</div>
  }

 return (
     <React.Fragment>

       <p className="mb-2 mt-0">
         <strong>Total:</strong> {numeral(logs.total).format("0,0")}
       </p>

       <table className="table table-sm table-hover table-striped mb-4 mt-3">
         <thead>
         <tr>
           <th style={{width: 200}}>Timestamp</th>
           <th>Message</th>
         </tr>
         </thead>
         <tbody>
         {logs.logs.map((log, i) => {
           return (
               <tr key={i}>
                 <td>{moment(log.timestamp).format()}</td>
                 <td>{log.message}</td>
               </tr>
           )
         })}
         </tbody>
       </table>

       <Paginator itemCount={logs.total} perPage={perPage} setPage={setPage} page={page} />
     </React.Fragment>
 )

}