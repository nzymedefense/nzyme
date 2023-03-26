import React from "react";
import MessageBusMessagesTable from "./MessageBusMessagesTable";

function MessageBusMessages() {

  return (
      <div className="row mt-3">
        <div className="col-md-12">
          <div className="card">
            <div className="card-body">
              <h3>Message Bus</h3>

              <MessageBusMessagesTable />
            </div>
          </div>
        </div>
      </div>
  )

}

export default MessageBusMessages