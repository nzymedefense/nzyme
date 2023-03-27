import React, {useEffect, useState} from "react";
import moment from "moment";
import ShortNodeId from "../../../../misc/ShortNodeId";
import Paginator from "../../../../misc/Paginator";
import LoadingSpinner from "../../../../misc/LoadingSpinner";
import ClusterService from "../../../../../services/ClusterService";
import TaskStatus from "./TaskStatus";

const clusterService = new ClusterService();

function TasksQueueTasksTable() {

  const [tasks, setTasks] = useState(null);
  const [page, setPage] = useState(1);
  const [localRevision, setLocalRevision] = useState(0);
  const perPage = 20;

  useEffect(() => {
    setTasks(null);
    clusterService.findTasksQueueTasks(setTasks, perPage, (page-1)*perPage);
  }, [page, localRevision])

  if (tasks === undefined || tasks === null) {
    return <LoadingSpinner />
  }

  if (tasks.tasks.length === 0) {
    return (
        <div className="alert alert-info">
          The tasks queue does not contain any tasks. (Note that retention cleaning deletes old tasks)
        </div>
    )
  }

  return (
      <React.Fragment>
        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th>ID</th>
            <th>Type</th>
            <th>Status</th>
            <th>Sender</th>
            <th>Processed By</th>
            <th>Created At</th>
            <th>Last Processed At</th>
          </tr>
          </thead>
          <tbody>
          {tasks.tasks.map((t, i) => {
            return (
                <tr key={"task-" + i}>
                  <td>{t.id}</td>
                  <td>{t.type}</td>
                  <td><TaskStatus task={t} setLocalRevision={setLocalRevision} /></td>
                  <td><ShortNodeId id={t.sender} /></td>
                  <td>{t.status === "PROCESSED_SUCCESS" && t.processed_by ? <ShortNodeId id={t.processed_by} /> : "n/a"}</td>
                  <td>{t.created_at ? moment(t.created_at).format() : "n/a"}</td>
                  <td>{t.last_processed_at ? moment(t.last_processed_at).format() : "n/a"}</td>
                </tr>
            )
          })}
          </tbody>
        </table>

        <Paginator itemCount={tasks.count} perPage={perPage} setPage={setPage} page={page} />
      </React.Fragment>
  )

}

export default TasksQueueTasksTable;