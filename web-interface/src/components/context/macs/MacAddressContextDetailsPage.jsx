import React, {useEffect, useState} from "react";
import WithPermission from "../../misc/WithPermission";
import ApiRoutes from "../../../util/ApiRoutes";
import {Navigate, useParams} from "react-router-dom";
import ContextService from "../../../services/ContextService";
import LoadingSpinner from "../../misc/LoadingSpinner";
import {notify} from "react-notify-toast";
import moment from "moment";
import ContextNotes from "../ContextNotes";
import TransparentContextSource from "../../shared/context/transparent/TransparentContextSource";
import TransparentIpAddressTable from "../../shared/context/transparent/TransparentIpAddressTable";
import TransparentHostnamesTable from "../../shared/context/transparent/TransparentHostnamesTable";

const contextService = new ContextService();

function MacAddressContextDetailsPage() {

  const {uuid} = useParams();
  const {organizationId} = useParams();
  const {tenantId} = useParams();

  const [context, setContext] = useState(null);

  const [deleted, setDeleted] = useState(false);

  useEffect(() => {
    contextService.findMacAddressContextByUuid(uuid, organizationId, tenantId, setContext);
  }, [uuid, organizationId, tenantId]);

  const onDelete = (e) => {
    e.preventDefault();

    if (!confirm("Really delete MAC address context?")) {
      return;
    }

    contextService.deleteMacAddressContext(uuid, organizationId, tenantId, () => {
      notify.show('MAC address context deleted.', 'success');
      setDeleted(true);
    });
  }

  if (deleted) {
    return <Navigate to={ApiRoutes.CONTEXT.MAC_ADDRESSES.INDEX} />
  }

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
                <li className="breadcrumb-item"><a href={ApiRoutes.CONTEXT.MAC_ADDRESSES.INDEX}>MAC Addresses</a></li>
                <li className="breadcrumb-item active">{context.mac_address}</li>
              </ol>
            </nav>
          </div>
        </div>

        <div className="row">
          <div className="col-md-8">
            <h1>
              Context of MAC Address &quot;{context.mac_address}&quot;{' '}

              {context.mac_address_is_randomized ?
                  <i className="fa-solid fa-triangle-exclamation text-danger cursor-help"
                     title="This is a randomized MAC address."/>
                  : null}
            </h1>
          </div>

          <div className="col-md-4">
            <span className="float-end">
              <WithPermission permission="mac_context_manage">
                <a className="btn btn-danger" href="#" onClick={onDelete}>Delete</a>&nbsp;
                <a className="btn btn-secondary"
                   href={ApiRoutes.CONTEXT.MAC_ADDRESSES.EDIT(context.uuid, context.organization_id, context.tenant_id)}>
                  Edit
                </a>&nbsp;
              </WithPermission>
              <a className="btn btn-primary" href={ApiRoutes.CONTEXT.MAC_ADDRESSES.INDEX}>Back</a>
            </span>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-8">
            <div className="card">
              <div className="card-body">
                <h3>Details</h3>

                <dl className="mb-0">
                  <dt>Name</dt>
                  <dd>{context.name ? <span className="context-name">{context.name}</span> :
                      <span className="text-muted">None</span>}</dd>
                  <dt>Description</dt>
                  <dd>{context.description}</dd>
                </dl>
              </div>
            </div>
          </div>

          <div className="col-4">
            <div className="card">
              <div className="card-body">
                <h3>Metadata</h3>

                <dl className="mb-0">
                  <dt>Created at</dt>
                  <dd title={context.created_at}>{moment(context.created_at).fromNow()}</dd>
                  <dt>Updated at</dt>
                  <dd title={context.updated_at}>{moment(context.updated_at).fromNow()}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-4">
            <div className="card">
              <div className="card-body">
                <h3>IP Addresses</h3>

                <TransparentIpAddressTable addresses={context.transparent_ip_addresses} />
              </div>
            </div>
          </div>

          <div className="col-4">
            <div className="card">
              <div className="card-body">
                <h3>Hostnames</h3>

                <TransparentHostnamesTable hostnames={context.transparent_hostnames} />
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-8">
            <div className="card">
              <div className="card-body">
                <h3>Notes</h3>

                <ContextNotes notes={context.notes}/>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default MacAddressContextDetailsPage;