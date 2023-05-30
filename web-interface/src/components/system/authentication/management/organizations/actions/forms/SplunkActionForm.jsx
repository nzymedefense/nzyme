import React from "react";

function SplunkActionForm() {

  return (
      <form className="mt-3">
        <div className="mb-3">
          <label htmlFor="hostname" className="form-label">Hostname</label>
          <input type="text" className="form-control" id="hostname" placeholder="splunk.example.org" />
        </div>
        <div className="mb-3">
          <label htmlFor="port" className="form-label">Port (TCP)</label>
          <input type="number" className="form-control" id="port" placeholder="12000" />
        </div>

        <button className="btn btn-primary">Create Action</button>
      </form>
  )

}

export default SplunkActionForm;