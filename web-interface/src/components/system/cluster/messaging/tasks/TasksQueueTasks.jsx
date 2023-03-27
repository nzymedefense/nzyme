import React from "react";
import TasksQueueTasksTable from "./TasksQueueTasksTable";

function TasksQueueTasks() {

  return (
      <div className="row mt-3">
        <div className="col-md-12">
          <div className="card">
            <div className="card-body">
              <h3>Tasks Queue</h3>

              <TasksQueueTasksTable />
            </div>
          </div>
        </div>
      </div>
  )

}

export default TasksQueueTasks