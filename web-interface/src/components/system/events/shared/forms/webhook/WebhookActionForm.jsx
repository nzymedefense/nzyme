import React, {useState} from "react";

export default function WebhookActionForm(props) {

  const buttonText = props.buttonText;
  const onSubmit = props.onSubmit;

  // For Edit/Update.
  const action = props.action;

  // Fields.
  const [name, setName] = useState(action && action.name ? action.name : "");
  const [description, setDescription] = useState(action && action.description ? action.description : "");
  const [url, setUrl] = useState(action && action.configuration.url ? action.configuration.url : "");
  const [allowInsecure, setAllowInsecure] = useState(action && action.configuration.allow_insecure ? action.configuration.allow_insecure : false);
  const [bearerToken, setBearerToken] = useState("");

  const [submitText, setSubmitText] = useState(buttonText);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const formReady = () => {
    return !isSubmitting
        && name && name !== ""
        && description && description !== ""
        && url && ((url.startsWith("http://") && url.length > 7) || (url.startsWith("https://") && url.length > 8))
  }

  const submit = () => {
    setIsSubmitting(true);
    setSubmitText("Submitting ... Please Wait")

    onSubmit(name, description, url, allowInsecure, bearerToken);
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
          Must start with <code>http://</code> or <code>https://</code>.
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
        <label htmlFor="bearerToken" className="form-label">Bearer Token <small>Optional</small></label>
        <input type="text"
               className="form-control"
               id="bearerToken"
               onChange={(e) => setBearerToken(e.target.value)} />
        <div className="form-text">
          Bearer token for authentication. Sent as HTTP header: <code>Authorization: Bearer &lt;token&gt;</code>.
          This value is encrypted in the Nzyme database.
        </div>
      </div>

      { action ? <div className="alert alert-warning">For security reasons, and, because the bearer token is encrypted in the Nzyme database, you must re-enter the token. Leaving the field blank will remove the token from this action.</div> : null}

      <button type="button"
              className="btn btn-primary"
              disabled={!formReady()}
              onClick={submit}>
        {submitText}
      </button>
    </form>
  )

}