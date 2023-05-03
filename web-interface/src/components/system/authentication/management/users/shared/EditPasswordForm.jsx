import React, {useEffect, useState} from "react";
import InlineFormValidationMessage from "../../../../../misc/InlineFormValidationMessage";

function EditPasswordForm(props) {

  const onClick = props.onClick;
  const submitText = props.submitText;

  const [password, setPassword] = useState("");

  const [passwordValidation, setPasswordValidation] = useState(undefined);
  const [formSubmitting, setFormSubmitting] = useState(false);

  useEffect(() => {
    if (password && (password.length < 12 || password.length > 128)) {
      setPasswordValidation("Must be between 12 and 128 characters long.");
    } else {
      setPasswordValidation(undefined);
    }
  }, [password])

  const updateValue = function(e, setter) {
    setter(e.target.value);
  }

  const formIsReady = function() {
    return password && password.trim().length > 0 && !passwordValidation
  }

  const submit = function(e) {
    e.preventDefault();

    if (!confirm("Really change password?")) {
      return;
    }

    setFormSubmitting(true);
    onClick(password, function() {
      setFormSubmitting(false);
    });
  }

  return (
      <form>
        <div className="mb-3">
          <label htmlFor="password" className="form-label">Password</label>
          <input type="password" className="form-control" id="password" aria-describedby="password"
                 autoComplete="new-password" value={password} onChange={(e) => { updateValue(e, setPassword) }} />
          <div className="form-text">
            The password of the user.{' '}
            <InlineFormValidationMessage message={passwordValidation} />
          </div>
        </div>

        <button className="btn btn-sm btn-primary" onClick={submit} disabled={!formIsReady() || formSubmitting}>
          {formSubmitting ? "Please wait ..." : submitText}
        </button>

      </form>
  )

}

export default EditPasswordForm;