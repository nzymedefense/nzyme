import React from "react";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import Paginator from "../../../misc/Paginator";
import moment from "moment";
import Dot11Service from "../../../../services/Dot11Service";
import {notify} from "react-notify-toast";

const dot11Service = new Dot11Service();

export default function KnownNetworksTable(props) {

    const networks = props.networks;
    const onChange = props.onChange;
    const page = props.page;
    const setPage = props.setPage;
    const perPage = props.perPage;

    const status = (network) => {
        if (network.is_approved) {
            return (
                <span>
                    <i className="fa fa-check text-success"/> Approved
                </span>
            )
        } else {
            if (network.is_ignored) {
                return (
                    <span>
                        <i className="fa fa-info-circle text-primary" /> Ignored
                    </span>
                )
            } else {
                return (
                    <span>
                        <i className="fa fa-warning text-warning"/> Not Approved
                    </span>
                )
            }
        }
    }

    const changeStatusLink = (network) => {
        if (network.is_approved) {
            return <a href="#" onClick={() => onRevoke(network)}>Revoke Approval</a>
        } else {
            return <a href="#" onClick={() => onApprove(network)}>Approve</a>
        }
    }

    const ignoreLink = (network) => {
        if (network.is_approved) {
            return <span className="text-muted" title="Approved networks cannot be ignored.">Ignore</span>
        } else {
            if (network.is_ignored) {
                return <a href="#" onClick={() => onUnignore(network)}>Unignore</a>
            } else {
                return <a href="#" onClick={() => onIgnore(network)}>Ignore</a>
            }
        }
    }

    const onApprove = (network) => {
        if (!confirm("Really approve network? You can revoke the approval at any time.")) {
            return;
        }

        dot11Service.approveKnownNetwork(network.uuid, network.organization_id, network.tenant_id, () => {
            notify.show('Known network approved.', 'success');
            onChange();
        });
    }

    const onRevoke = (network) => {
        if (!confirm("Really revoke approval? You can re-approve it at any time.")) {
            return;
        }

        dot11Service.revokeKnownNetwork(network.uuid, network.organization_id, network.tenant_id, () => {
            notify.show('Known network approval revoked.', 'success');
            onChange();
        });
    }


    const onIgnore = (network) => {
        if (!confirm("Really ignore network? You can un-ignore it at any time.")) {
            return;
        }

        dot11Service.ignoreKnownNetwork(network.uuid, network.organization_id, network.tenant_id, () => {
            notify.show('Known network ignored.', 'success');
            onChange();
        });
    }

    const onUnignore = (network) => {
        if (!confirm("Really unignore network? You can ignore it again at any time.")) {
            return;
        }

        dot11Service.unignoreKnownNetwork(network.uuid, network.organization_id, network.tenant_id, () => {
            notify.show('Known network unignored.', 'success');
            onChange();
        });
    }

    const onDelete = (e, network) => {
        e.preventDefault();

        if (!confirm("Really delete known network? It will reappear as unapproved network " +
            "next time nzyme records it.")) {
            return;
        }

        dot11Service.deleteKnownNetwork(network.uuid, network.organization_id, network.tenant_id, () => {
            notify.show('Known network deleted.', 'success');
            onChange();
        });
    }

    if (!networks) {
        return <LoadingSpinner />
    }

    if (networks.networks.length === 0) {
        return <div className="alert alert-warning mt-3">
            No known networks discovered yet. Make sure that SSID monitoring is enabled and that networks are in range.
            Note that it will take at least 5 minutes to discover known networks after SSID monitoring is enabled.
        </div>
    }

    return (
        <React.Fragment>

            <table className="table table-sm table-hover table-striped">
                <thead>
                <tr>
                    <th>SSID</th>
                    <th>Status</th>
                    <th>Last Seen</th>
                    <th>&nbsp;</th>
                    <th>&nbsp;</th>
                    <th>&nbsp;</th>
                </tr>
                </thead>
                <tbody>
                {networks.networks.map((net, i) => {
                    return (
                        <tr key={i}>
                            <td>{net.ssid}</td>
                            <td>{status(net)}</td>
                            <td title={moment(net.last_seen).format()}>{moment(net.last_seen).fromNow()}</td>
                            <td>{changeStatusLink(net)}</td>
                            <td>{ignoreLink(net)}</td>
                            <td>
                                <a href="#" onClick={(e) => onDelete(e, net)}>
                                    Delete
                                </a>
                            </td>
                        </tr>
                    )
                })}
                </tbody>
            </table>

            <Paginator page={page} setPage={setPage} perPage={perPage} itemCount={networks.total}/>
        </React.Fragment>
)

}