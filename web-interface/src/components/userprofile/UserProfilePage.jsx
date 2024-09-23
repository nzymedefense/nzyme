import React, {useContext, useState} from "react";
import UserProfile from "./UserProfile";
import MfaRecoveryCodes from "./MfaRecoveryCodes";
import UserProfileService from "../../services/UserProfileService";
import {notify} from "react-notify-toast";
import ApiRoutes from "../../util/ApiRoutes";
import WithExactRole from "../misc/WithExactRole";
import WithMinimumRole from "../misc/WithMinimumRole";
import OrganizationAndTenantSelector from "../shared/OrganizationAndTenantSelector";
import {UserContext} from "../../App";

const userProfileService = new UserProfileService();

function UserProfilePage(props) {

  const user = useContext(UserContext);

  const onMfaReset = props.onMfaReset;
  const [showRecoveryCodes, setShowRecoveryCodes] = useState(false);

  const [defaultTenant, setDefaultTenant] = useState(null);
  const [defaultOrganization, setDefaultOrganization] = useState(null);

  const resetMfa = function() {
    if (!confirm("Really reset your MFA? You will be logged out and prompted to set up a new MFA method " +
        "after you log in again.")) {
      return;
    }

    userProfileService.resetOwnMfa(function () {
      notify.show('MFA successfully reset.', 'success');
      onMfaReset();
    })
  }

  const onSaveDefaultTenant = (e) => {
    e.preventDefault();

    userProfileService.setDefaultTenant(defaultOrganization, defaultTenant, () => {
      notify.show('Default tenant updated.', 'success');
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

                    <UserProfile/>

                    <a className="btn btn-sm btn-secondary mt-2" href={ApiRoutes.USERPROFILE.PASSWORD}>
                      Change Password
                    </a>
                  </div>
                </div>
              </div>
            </div>

            <div className="row mt-3">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <h3>Multi-Factor Authentication</h3>

                    { /* We can show this statically because if you reached this, your MFA is set up and active. */}
                    <div className="alert alert-success">
                      <i className="fa fa-check-circle"/>{' '}
                      Your account is protected by multi-factor authentication.
                    </div>

                    <hr/>

                    <h4>Reset MFA</h4>
                    <p>
                      You can reset your own multi-factor authentication. After the reset, you will be logged out and
                      prompted to set up a new MFA method after you log in again.
                    </p>

                    <button className="btn btn-sm btn-secondary" onClick={resetMfa}>
                      Reset my MFA configuration
                    </button>

                    <hr/>

                    <h4>Recovery Codes</h4>
                    <p>
                      If you lose access to your code generator, you can use each recovery code <strong>once</strong> to
                      pass multi-factor authentication challenges. Store these keys in a safe place and do not share
                      them with anyone.
                    </p>

                    <MfaRecoveryCodes show={showRecoveryCodes}/>

                    <button className="btn btn-sm btn-secondary mt-2"
                            onClick={() => setShowRecoveryCodes(!showRecoveryCodes)}>
                      {showRecoveryCodes ? "Hide Codes" : "Show Codes"}
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <WithMinimumRole role="ORGADMIN">
              <div className="row mt-3">
                <div className="col-md-12">
                  <div className="card">
                    <div className="card-body">
                      <h3>Default Tenant</h3>

                      <p>
                        You can select a default organization or tenant that will be automatically selected across the
                        web interface when such a selection is required.
                      </p>

                      <div className="mt-2">
                        <OrganizationAndTenantSelector
                            organizationSelectorTitle={<strong>Default Organization</strong>}
                            tenantSelectorTitle={<strong>Default Tenant</strong>}
                            emptyOrganizationTitle="None"
                            emptyTenantTitle="None"
                            onOrganizationChange={(org) => setDefaultOrganization(org)}
                            onTenantChange={(tenant) => setDefaultTenant(tenant)} />
                      </div>

                      <div className="mt-2">
                        <button className="btn btn-sm btn-secondary" onClick={onSaveDefaultTenant}>Save</button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </WithMinimumRole>
          </div>

          <div className="col-md-4">
            <div className="row">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    For security reasons, users can manage their own password and MFA but not edit their name or
                    email address. Please contact your administrator to perform such a change.
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