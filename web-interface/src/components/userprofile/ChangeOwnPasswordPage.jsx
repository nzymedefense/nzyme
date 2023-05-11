import React, {useEffect, useState} from "react";
import UserProfileService from "../../services/UserProfileService";
import Routes from "../../util/ApiRoutes";

import InlineFormValidationMessage from "../misc/InlineFormValidationMessage";
import FormSubmitErrorMessage from "../misc/FormSubmitErrorMessage";
import {notify} from "react-notify-toast";

const userProfileService = new UserProfileService();

function ChangeOwnPasswordPage() {

  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");

  const [passwordValidation, setPasswordValidation] = useState(undefined);
  const [formSubmitting, setFormSubmitting] = useState(false);

  const [errorMessage, setErrorMessage] = useState(null)

  useEffect(() => {
    if (newPassword && (newPassword.length < 12 || newPassword.length > 128)) {
      setPasswordValidation("Must be between 12 and 128 characters long.");
    } else {
      setPasswordValidation(undefined);
    }
  }, [newPassword])

  const onSubmit = function(e) {
    e.preventDefault();

    setErrorMessage(null);
    setFormSubmitting(true);

    userProfileService.changeOwnPassword(currentPassword, newPassword, function() {
      notify.show('Password updated.', 'success');
    }, function(error) {
      setFormSubmitting(false);
      setErrorMessage(error.response.data.message);
    })
  }

  const updateValue = function(e, setter) {
    setter(e.target.value);
  }

  const formIsReady = function() {
    return currentPassword && currentPassword.trim().length > 0 &&
        newPassword && newPassword.trim().length > 0 && !passwordValidation
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-8">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item">
                  <a href={Routes.USERPROFILE.PROFILE}>Your Profile</a>
                </li>
                <li className="breadcrumb-item active" aria-current="page">Change your password</li>
              </ol>
            </nav>
          </div>
        </div>

        <div className="row">
          <div className="col-md-8">
            <div className="row">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <h3>Change your password</h3>

                    <form>
                      <div className="mb-3">
                        <label htmlFor="currentPassword" className="form-label">Current Password</label>
                        <input type="password" className="form-control" id="currentPassword" aria-describedby="currentPassword"
                               value={currentPassword} onChange={(e) => { updateValue(e, setCurrentPassword) }} />
                        <div className="form-text">
                          Your current password
                        </div>
                      </div>

                      <div className="mb-3">
                        <label htmlFor="password" className="form-label">New Password</label>
                        <input type="password" className="form-control" id="password" aria-describedby="password"
                               autoComplete="new-password" value={newPassword} onChange={(e) => { updateValue(e, setNewPassword) }} />
                        <div className="form-text">
                          Your new password{' '}
                          <InlineFormValidationMessage message={passwordValidation} />
                        </div>
                      </div>

                      <div className="alert alert-info">
                        You will be logged out and prompted to log in again after changing your password.
                      </div>

                      <button className="btn btn-sm btn-primary" onClick={onSubmit} disabled={!formIsReady() || formSubmitting}>
                        {formSubmitting ? "Please wait ..." : "Change Password"}
                      </button>

                      <FormSubmitErrorMessage message={errorMessage} />
                    </form>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default ChangeOwnPasswordPage;