import React, {useState} from "react";

function UserForm(props) {

  const onClick = props.onClick;
  const submitText = props.submitText;

  const [email, setEmail] = useState(props.email ? props.email : "");
  const [password, setPassword] = useState("");
  const [name, setName] = useState(props.name ? props.name : "");

  const updateValue = function(e, setter) {
    setter(e.target.value);
  }

  const formIsReady = function() {
    return email && email.trim().length > 0 && password && password.trim().length > 0 && name && name.trim().length > 0
  }

  // validate password. see ticket, trim()/no leading/trailing whitepsace. password confirm / second time etc

  const submit = function(e) {
    e.preventDefault();
    onClick(email, password, name);
  }

  return (
      <form>
        <div className="mb-3">
          <label htmlFor="email" className="form-label">Email Address / Username</label>
          <input type="email" className="form-control" id="email" aria-describedby="email"
                 value={email} onChange={(e) => { updateValue(e, setEmail) }} />
          <div className="form-text">The email address of the new user. This will be the username.</div>
        </div>

        <div className="mb-3">
          <label htmlFor="password" className="form-label">Password</label>
          <input type="password" className="form-control" id="password" aria-describedby="password"
                 autocomplete="new-password" value={password} onChange={(e) => { updateValue(e, setPassword) }} />
          <div className="form-text">The password address of the new user.</div>
        </div>

        <div className="mb-3">
          <label htmlFor="name" className="form-label">Full Name</label>
          <input type="email" className="form-control" id="name" aria-describedby="name"
                 value={name} onChange={(e) => { updateValue(e, setName) }} />
          <div className="form-text">The full name of the new user.</div>
        </div>

        <button className="btn btn-sm btn-primary" onClick={submit} disabled={!formIsReady()}>
          {submitText}
        </button>
      </form>
  )

}

export default UserForm;