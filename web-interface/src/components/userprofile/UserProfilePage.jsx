import React, {useState} from "react";
import UserProfile from "./UserProfile";
import MfaRecoveryCodes from "./MfaRecoveryCodes";
import UserProfileService from "../../services/UserProfileService";
import {notify} from "react-notify-toast";

const userProfileService = new UserProfileService();

function UserProfilePage() {

  const [showRecoveryCodes, setShowRecoveryCodes] = useState(false);

  const resetMfa = function() {
    if (!confirm("Really reset your MFA? You will be logged out and prompted to set up a new MFA method " +
        "after you log in again.")) {
      return;
    }

    userProfileService.resetOwnMfa(function () {
      notify.show('MFA successfully reset.', 'success');
    })
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-12">
            <h1>Your User Profile</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-8">
            <div className="row">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <h3>User Data</h3>

                    <UserProfile />
                  </div>
                </div>
              </div>
            </div>

            <div className="row mt-3">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <h3>Multi-Factor Authentication</h3>

                    { /* We can show this statically because if you reached this, your MFA is set up and active. */ }
                    <div className="alert alert-success">
                      <i className="fa fa-check-circle" />{' '}
                      Your account is protected by multi-factor authentication.
                    </div>

                    <hr />

                    <h4>Reset MFA</h4>
                    <p>
                      You can reset your own multi-factor authentication. After the reset, you will be logged out and
                      prompted to set up a new MFA method after you log in again.
                    </p>

                    <button className="btn btn-sm btn-secondary" onClick={resetMfa}>
                      Reset my MFA configuration
                    </button>

                    <hr />

                    <h4>Recovery Codes</h4>
                    <p>
                      If you lose access to your code generator, you can use each recovery code <strong>once</strong> to
                      pass multi-factor authentication challenges. Store these keys in a safe place and do not share
                      them with anyone.
                    </p>

                    <MfaRecoveryCodes show={showRecoveryCodes} />

                    <button className="btn btn-sm btn-secondary mt-2"
                            onClick={() => setShowRecoveryCodes(!showRecoveryCodes)}>
                      {showRecoveryCodes ? "Hide Codes" : "Show Codes"}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

      </React.Fragment>
  )

}

export default UserProfilePage;