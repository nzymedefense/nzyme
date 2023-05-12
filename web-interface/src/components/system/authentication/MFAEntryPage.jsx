import React, {useState} from "react";
import AssetStylesheet from "../../misc/AssetStylesheet";
import MFAEntryStep from "./MFAEntryStep";
import MFARecoveryCodeStep from "./MFARecoveryCodeStep";

function MFASetupPage(props) {

  const mfaEntryExpiresAt = props.mfaEntryExpiresAt;

  const [recoveryEnabled, setRecoveryEnabled] = useState(false);

  const onEnableRecovery = function() {
    setRecoveryEnabled(true);
  }

  const onAbortRecovery = function() {
    setRecoveryEnabled(false);
  }

  return (
      <React.Fragment>
        <AssetStylesheet filename="onebox.css"/>

        <section className="vh-100 start">
          <div className="container py-5 h-100 mb-5">
            <div className="row d-flex justify-content-center align-items-center h-100">
              <div className="col col-xl-10">
                <div className="card main-card">
                  <div className="row g-0 vh-100">

                    <div className="col-md-5 d-flex align-items-center">
                      <div className="card-body p-4 p-lg-5 text-black">

                        <MFAEntryStep show={!recoveryEnabled}
                                      mfaEntryExpiresAt={mfaEntryExpiresAt}
                                      onEnableRecovery={onEnableRecovery} />

                        <MFARecoveryCodeStep show={recoveryEnabled} onAbort={onAbortRecovery} />
                      </div>
                    </div>

                    <div className="col-md-7 d-none d-md-block justify-content-center right-half">
                      <video id="background-video" autoPlay loop muted
                             poster={window.appConfig.assetsUri + "static/loginsplash_preview.jpg"}>
                        <source src={window.appConfig.assetsUri + "static/loginsplash.mp4"} type="video/mp4"/>
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

export default MFASetupPage;