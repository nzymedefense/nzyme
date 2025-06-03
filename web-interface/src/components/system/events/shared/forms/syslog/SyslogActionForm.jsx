import React, {useState} from "react";

export default function SyslogActionForm(props) {

  const buttonText = props.buttonText;
  const onSubmit = props.onSubmit;

  // For Edit/Update.
  const action = props.action;

  // Fields.
  const [name, setName] = useState(action && action.name ? action.name : "");
  const [description, setDescription] = useState(action && action.description ? action.description : "");
  const [protocol, setProtocol] = useState(action && action.configuration.protocol ? action.configuration.protocol : "UDP_RFC5424");
  const [syslogHostname, setSyslogHostname] = useState(action && action.configuration.syslog_hostname ? action.configuration.syslog_hostname : "nzyme");
  const [host, setHost] = useState(action && action.configuration.host ? action.configuration.host : "");
  const [port, setPort] = useState(action && action.configuration.port ? action.configuration.port : 514);

  const [submitText, setSubmitText] = useState(buttonText);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const formReady = () => {
    return !isSubmitting
        && name && name !== ""
        && description && description !== ""
        && protocol && protocol !== ""
        && syslogHostname && syslogHostname !== ""
        && host && host !== ""
        && port && port > 0 && port <= 65535;
  }

  const submit = () => {
    setIsSubmitting(true);
    setSubmitText("Submitting ... Please Wait")

    onSubmit(name, description, protocol, syslogHostname, host, port);
  }

  return (
      <form className="mt-3">
        <div className="mb-3">
          <label htmlFor="name" className="form-label">Action Name</label>
          <input type="text"
                 className="form-control"
                 id="name"
                 value={name}
                 onChange={(e) => setName(e.target.value)} />
          <div className="form-text">
            A short description of this action to help others quickly identify it.
          </div>
        </div>

        <div className="mb-3">
          <label htmlFor="description" className="form-label">Description</label>
          <textarea className="form-control" id="description" rows="3"
                    value={description} onChange={(e) => setDescription(e.target.value)} />
          <div className="form-text">
            A short description of this action to help others understand what it does.
          </div>
        </div>

        <div className="mb-3">
          <label htmlFor="protocol" className="form-label">Protocol</label>
          <select id="actiontype"
                  className="form-select"
                  value={protocol} onChange={(e) => setProtocol(e.target.value)}>
            <option value="UDP_RFC5424">UDP Syslog (RFC 5424)</option>
          </select>
        </div>

        <div className="mb-3">
          <label htmlFor="syslog_hostname" className="form-label">Syslog Hostname</label>
          <input type="text"
                 className="form-control"
                 id="syslog_hostname"
                 value={syslogHostname}
                 onChange={(e) => setSyslogHostname(e.target.value)} />
          <div className="form-text">
            Value to use as <em>Hostname</em> in the generated Syslog message. This can be useful to identify a Nzyme
            setup if you run multiple installations.
          </div>
        </div>

        <div className="mb-3">
          <label htmlFor="destination_host" className="form-label">Destination Host</label>
          <input type="text"
                 className="form-control"
                 id="destination_host"
                 value={host}
                 onChange={(e) => setHost(e.target.value)} />
          <div className="form-text">
            Name or IP address of host to send Syslog messages to.
          </div>
        </div>

        <div className="mb-3">
          <label htmlFor="destination_host" className="form-label">Destination Port</label>
          <input type="number"
                 min={1}
                 max={65535}
                 className="form-control"
                 id="port"
                 value={port}
                 onChange={(e) => setPort(e.target.value ? parseInt(e.target.value, 10) : "")} />
          <div className="form-text">
            Syslog destination port number.
          </div>
        </div>

        <button type="button"
                className="btn btn-primary"
                disabled={!formReady()}
                onClick={submit}>
          {submitText}
        </button>
      </form>
  )

}