import React, {useEffect, useState} from "react";
import InlineFormValidationMessage from "../../../../misc/InlineFormValidationMessage";
import FormSubmitErrorMessage from "../../../../misc/FormSubmitErrorMessage";

function EditTenantUserForm(props) {

  const EMAILREGEX = /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i;

  const onClick = props.onClick;
  const submitText = props.submitText;
  const errorMessage = props.errorMessage;

  const [email, setEmail] = useState(props.email ? props.email : "");
  const [name, setName] = useState(props.name ? props.name : "");

  const [emailValidation, setEmailValidation] = useState(undefined);
  const [formSubmitting, setFormSubmitting] = useState(false);

  useEffect(() => {
    if (email && !EMAILREGEX.test(email)) {
      setEmailValidation("Must be a valid email address.");
    } else {
      setEmailValidation(undefined);
    }
  }, [email])

  const updateValue = function(e, setter) {
    setter(e.target.value);
  }

  const formIsReady = function() {
    return email && email.trim().length > 0 && name && name.trim().length > 0
        && !emailValidation
  }

  const submit = function(e) {
    e.preventDefault();
    setFormSubmitting(true);
    onClick(email, name, function() {
      setFormSubmitting(false);
    });
  }

  return (
      <form>
        <div className="mb-3">
          <div className="mb-3">
            <label htmlFor="name" className="form-label">Full Name</label>
            <input type="email" className="form-control" id="name" aria-describedby="name"
                   value={name} onChange={(e) => { updateValue(e, setName) }} />
            <div className="form-text">The full name of the user.</div>
          </div>

          <label htmlFor="email" className="form-label">Email Address / Username</label>
          <input type="email" className="form-control" id="email" aria-describedby="email"
                 value={email} onChange={(e) => { updateValue(e, setEmail) }} />
          <div className="form-text">
            The email address of the user. This is also the username.{' '}
            <InlineFormValidationMessage message={emailValidation} />
          </div>
        </div>

        <button className="btn btn-sm btn-primary" onClick={submit} disabled={!formIsReady() || formSubmitting}>
          {formSubmitting ? "Please wait ..." : submitText}
        </button>

        <FormSubmitErrorMessage message={errorMessage} />
      </form>
  )

}

export default EditTenantUserForm;