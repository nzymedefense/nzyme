import React, {useEffect, useState} from "react";
import moment from "moment";
import ShortNodeId from "../../../../misc/ShortNodeId";
import Paginator from "../../../../misc/Paginator";
import LoadingSpinner from "../../../../misc/LoadingSpinner";
import ClusterService from "../../../../../services/ClusterService";
import MessageStatus from "./MessageStatus";
import {notify} from "react-notify-toast";

const clusterService = new ClusterService();

function MessageBusMessagesTable() {

  const [messages, setMessages] = useState(null);
  const [localRevision, setLocalRevision] = useState(0);
  const [page, setPage] = useState(1);
  const perPage = 20;

  useEffect(() => {
    setMessages(null);
    clusterService.findMessageBusMessages(setMessages, perPage, (page-1)*perPage);
  }, [page, localRevision])

  const markAllAck = function() {
    if (!confirm("Really mark all failures as acknowledged?")) {
      return;
    }

    clusterService.acknowledgeAllFailedMessages(() => {
      notify.show("Failures acknowledged.", "success");
      setLocalRevision(prevRev => prevRev + 1);
    })
  }

  if (messages === undefined || messages === null) {
    return <LoadingSpinner />
  }

  if (messages.messages.length === 0) {
    return (
        <div className="alert alert-info">
          The message bus has not processed any messages. (Note that retention cleaning deletes old messages)
        </div>
    )
  }

  return (
      <React.Fragment>
        <button className="btn btn-sm btn-secondary mb-2" onClick={markAllAck}>
          Mark all failed messages as acknowledged
        </button>

        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th>ID</th>
            <th>Type</th>
            <th>Status</th>
            <th>Sender</th>
            <th>Receiver</th>
            <th>Sent At</th>
            <th>Cycle Limiter</th>
            <th>Acknowledged At</th>
          </tr>
          </thead>
          <tbody>
          {messages.messages.map((m, i) => {
            return (
                <tr key={"message-" + i}>
                  <td>{m.id}</td>
                  <td>{m.type}</td>
                  <td><MessageStatus message={m} setLocalRevision={setLocalRevision} /></td>
                  <td><ShortNodeId id={m.sender}/></td>
                  <td><ShortNodeId id={m.receiver}/></td>
                  <td>{m.created_at ? moment(m.created_at).format() : "n/a"}</td>
                  <td>{m.cycle_limiter}</td>
                  <td>{m.acknowledged_at ? moment(m.acknowledged_at).format() : "n/a"}</td>
                </tr>
            )
          })}
          </tbody>
        </table>

        <Paginator itemCount={messages.count} perPage={perPage} setPage={setPage} page={page} />
      </React.Fragment>
  )

}

export default MessageBusMessagesTable;