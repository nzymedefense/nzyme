import React from "react";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import L4SessionState from "./L4SessionState";
import InternalAddressOnlyWrapper from "../shared/InternalAddressOnlyWrapper";
import EthernetMacAddress from "../../shared/context/macs/EthernetMacAddress";
import L4SessionTags from "./L4SessionTags";

export default function SessionDetails({type, session}) {

  return (
      <React.Fragment>
        <div className="row mt-3">
          <div className="col-md-4">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Overview" />

                <dl className="mb-0">
                  <dt>Source MAC</dt>
                  <dd>
                    <InternalAddressOnlyWrapper
                      address={session.source}
                      inner={<EthernetMacAddress
                          addressWithContext={session.source.mac}
                          assetId={session.source && session.source.asset_id ? session.source.asset_id : null}
                          withAssetLink withAssetName />} />
                  </dd>
                  <dt>Source Address</dt>
                  <dd></dd>
                  <dt>Destination MAC</dt>
                  <dd></dd>
                  <dt>Destination Address</dt>
                  <dd></dd>
                  <dt>State</dt>
                  <dd><L4SessionState state={session.state} showFull={true} /></dd>
                  <dt>Tags</dt>
                  <dd><L4SessionTags tags={session.tags} /></dd>
                </dl>
              </div>
            </div>
          </div>

          <div className="col-md-4">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Data" />

                <dl className="mb-0">
                  <dt>Total Traffic Exchanged</dt>
                  <dd></dd>
                  <dt>RX Traffic</dt>
                  <dd></dd>
                  <dt>TX Traffic</dt>
                  <dd></dd>
                  <dt>Segments</dt>
                  <dd></dd>
                </dl>
              </div>
            </div>
          </div>

          <div className="col-md-4">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Timing" />

                <dl className="mb-0">
                  <dt>Start Time</dt>
                  <dd></dd>
                  <dt>End Time</dt>
                  <dd></dd>
                  <dt>Duration</dt>
                  <dd></dd>
                  <dt>Most Recent Segment Time</dt>
                  <dd></dd>
                </dl>
              </div>
            </div>
          </div>

        </div>
      </React.Fragment>
  )

}