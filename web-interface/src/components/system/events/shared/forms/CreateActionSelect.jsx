import React from "react";

function CreateActionSelect(props) {

  const type = props.type;
  const setType = props.setType;

  return (
      <React.Fragment>
        <label htmlFor="actiontype" className="form-label">Action Type</label>
        <select id="actiontype"
                className="form-select"
                value={type} onChange={(e) => setType(e.target.value)}>
          <option value="">Please select an action type</option>
          <option value="email">Send email</option>
          <option value="splunk_message">Send Splunk message</option>
          <option value="opensearch_message">Send OpenSearch message</option>
          <option value="graylog_message">Send Graylog message</option>
          <option value="wasm_exec">Execute WASM binary</option>
        </select>
      </React.Fragment>
  )

}

export default CreateActionSelect;