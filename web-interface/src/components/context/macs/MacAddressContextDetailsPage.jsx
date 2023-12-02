import React, {useEffect, useState} from "react";
import WithPermission from "../../misc/WithPermission";
import ApiRoutes from "../../../util/ApiRoutes";
import {useParams} from "react-router-dom";
import ContextService from "../../../services/ContextService";
import LoadingSpinner from "../../misc/LoadingSpinner";

const contextService = new ContextService();

function MacAddressContextDetailsPage() {

  const {uuid} = useParams();
  const {organizationId} = useParams();
  const {tenantId} = useParams();

  const [context, setContext] = useState(null);

  useEffect(() => {
    contextService.findMacAddressContextByUuid(uuid, organizationId, tenantId, setContext);
  }, [uuid, organizationId, tenantId]);

  if (!context) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-8">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item">Context</li>
                <li className="breadcrumb-item">MAC Addresses</li>
                <li className="breadcrumb-item active">{context.mac_address}</li>
              </ol>
            </nav>
          </div>
        </div>

        <div className="row">
          <div className="col-md-8">
            <h1>
              Context: MAC Address &quot;{context.mac_address}&quot;
            </h1>
          </div>

          <div className="col-md-4">
            <span className="float-end">
              <WithPermission permission="mac_aliases_manage">
                <a className="btn btn-danger" href="">Delete Context</a>&nbsp;
              </WithPermission>
              <a className="btn btn-primary" href={ApiRoutes.CONTEXT.MAC_ADDRESSES.INDEX}>Back</a>
            </span>
          </div>
        </div>
      </React.Fragment>
  )

}

export default MacAddressContextDetailsPage;