import React, {useState} from "react";

export default function WebhookActionForm(props) {

  const buttonText = props.buttonText;
  const onSubmit = props.onSubmit;

  // For Edit/Update.
  const action = props.action;

  // Fields.
  const [name, setName] = useState(action && action.name ? action.name : "");
  const [description, setDescription] = useState(action && action.description ? action.description : "");
  const [url, setUrl] = useState(action && action.url ? action.url : "");
  const [allowInsecure, setAllowInsecure] = useState(action && action.allowInsecure ? action.allowInsecure : false);
  const [bearerToken, setBearerToken] = useState(action && action.bearerToken ? action.bearerToken : "");

  const [submitText, setSubmitText] = useState(buttonText);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const formReady = function() {
    return !isSubmitting
      && false
  }

  const submit = function() {
    setIsSubmitting(true);
    setSubmitText("Submitting ... Please Wait")

    //onSubmit(name, description, subjectPrefix, receivers);
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
        <label htmlFor="url" className="form-label">Webhook URL</label>
        <input type="text"
               className="form-control"
               id="url"
               value={url}
               onChange={(e) => setUrl(e.target.value)} />
        <div className="form-text">
          Event payloads will be sent to this URL via HTTP POST request.
        </div>
      </div>

      <div className="mb-3">
        <div className="form-check">
          <input type="checkbox"
                 className="form-check-input"
                 id="allowInsecure"
                 checked={allowInsecure}
                 onChange={(e) => setAllowInsecure(e.target.checked)} />
          <label htmlFor="allowInsecure" className="form-check-label">Allow Insecure Connections</label>
          <div className="form-text">
            Allow insecure HTTPS connections (e.g., self-signed or invalid certificates)?
          </div>
        </div>
      </div>

      <div className="mb-3">
        <label htmlFor="bearerToken" className="form-label">Bearer Token</label>
        <input type="text"
               className="form-control"
               id="bearerToken"
               value={bearerToken}
               onChange={(e) => setBearerToken(e.target.value)} />
        <div className="form-text">
          Optional Bearer token for authentication. Sent as HTTP header: <code>Authorization: Bearer &lt;token&gt;</code>.
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