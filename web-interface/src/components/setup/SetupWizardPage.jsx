  import React, {useState} from "react";
import AssetStylesheet from "../misc/AssetStylesheet";
import CreateUserForm from "../system/authentication/management/users/shared/CreateUserForm";
import {notify} from "react-notify-toast";
import AuthenticationManagementService from "../../services/AuthenticationManagementService";

const authenticationMgmtService = new AuthenticationManagementService();

function SetupWizardPage() {

  const [errorMessage, setErrorMessage] = useState(null);

  const onCreateUserClick = function (email, password, name, callback) {
    authenticationMgmtService.createInitialUser(email, password, name, function() {
      // Success.
      notify.show('First Super Administrator created. Please log in.', 'success');
      callback();
    }, function (error) {
      // Error.
      setErrorMessage(error.response.data.message);
      callback();
    })
  }

  return (
      <React.Fragment>
        <AssetStylesheet filename="onebox.css" />

        <section className="vh-100 start setupwizard">
          <div className="container py-5 vh-100 mb-5">
            <div className="row d-flex justify-content-center align-items-center h-100">
              <div className="col col-xl-10">
                <div className="card main-card">
                  <div className="row g-0 vh-100">
                    <div className="col-md-5 d-flex">
                      <div className="card-body p-4 p-lg-5 text-black">
                        <h1 className="mb-3 pb-3">Welcome.</h1>

                        <p>
                          This setup wizard will guide you through the few initial setups required to get started
                          with nzyme.
                        </p>

                        <hr />

                        <h2>First Super Administrator User</h2>

                        <p>
                          Super Administrators possess unrestricted access to all features and data within this nzyme
                          installation. While this role is ideal for initial setup, it is recommended to transition
                          to a user with tailored permissions for your everyday tasks.
                        </p>

                        <CreateUserForm onClick={onCreateUserClick}
                                        errorMessage={errorMessage}
                                        submitText="Create Super Administrator" />
                      </div>
                    </div>
                    <div className="col-md-7 d-none d-md-block justify-content-center right-half">
                      <video id="background-video" autoPlay loop muted poster={window.appConfig.assetsUri + "static/loginsplash_preview.jpg"}>
                        <source src={window.appConfig.assetsUri + "static/loginsplash.mp4"} type="video/mp4" />
                      </video>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </React.Fragment>
  )

}

export default SetupWizardPage;