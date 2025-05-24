import React, {useContext, useEffect, useState} from "react";
import {TapContext} from "../../../../App";
import {disableTapSelector, enableTapSelector} from "../../../misc/TapSelector";
import AlphaFeatureAlert from "../../../shared/AlphaFeatureAlert";
import SectionMenuBar from "../../../shared/SectionMenuBar";
import {ASSETS_MENU_ITEMS} from "../AssetsMenuItems";
import ApiRoutes from "../../../../util/ApiRoutes";
import {useLocation, useParams} from "react-router-dom";
import AssetsService from "../../../../services/ethernet/AssetsService";
import moment from "moment";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import CardTitleWithControls from "../../../shared/CardTitleWithControls";
import {Presets} from "../../../shared/timerange/TimeRange";
import DHCPTransactionSuccess from "./DHCPTransactionSuccess";
import DHCPDuration from "./DHCPDuration";
import DHCPNotes from "./DHCPNotes";

const useQuery = () => {
  return new URLSearchParams(useLocation().search);
}

const assetsService = new AssetsService();

export default function DHCPTransactionDetailsPage() {

  let urlQuery = useQuery();
  const tapContext = useContext(TapContext);

  const { transactionId } = useParams();
  const selectedTaps = tapContext.taps;

  const [transactionTime, ignored] = useState(
      urlQuery.get("transaction_time") ? urlQuery.get("transaction_time") : null
  );

  const [tx, setTx] = useState(null);

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

  useEffect(() => {
    setTx(null);
    assetsService.findDHCPTransaction(transactionId, transactionTime, selectedTaps, setTx)
  }, [transactionId, transactionTime])

  if (!transactionTime) {
    return <div className="alert alert-danger">No Transaction Time passed.</div>
  }

  if (!tx) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <AlphaFeatureAlert />

        <div className="row">
          <div className="col-md-12">
            <SectionMenuBar items={ASSETS_MENU_ITEMS}
                            activeRoute={ApiRoutes.ETHERNET.ASSETS.DHCP.INDEX} />
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-8">
            <h1>DHCP Transaction &quot;{transactionId}&quot; <small>at {moment(transactionTime).format()}</small></h1>
          </div>

          <div className="col-md-4 text-end">
            <a href={ApiRoutes.ETHERNET.ASSETS.DHCP.INDEX} className="btn btn-primary">Back to Transactions</a>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Transaction Information" />

                <dl>
                  <dt>ID</dt>
                  <dd>{tx.transaction_id}</dd>
                  <dt>Type</dt>
                  <dd>{tx.transaction_type}</dd>
                  <dt>Complete</dt>
                  <dd>{tx.is_complete ? <span className="text-success">Complete</span>
                      : <span className="text-warning">Incomplete</span>}</dd>
                  <dt>Successful</dt>
                  <dd><DHCPTransactionSuccess success={tx.is_successful} /></dd>
                </dl>
              </div>
            </div>
          </div>

          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Timing" />

                <dl>
                  <dt>First Packet</dt>
                  <dd>
                    {moment(tx.first_packet).format("YYYY-MM-DDTHH:mm:ss.SSSZ")} ({moment(tx.first_packet).fromNow()})
                  </dd>
                  <dt>Latest Packet</dt>
                  <dd>
                    {moment(tx.latest_packet).format("YYYY-MM-DDTHH:mm:ss.SSSZ")} ({moment(tx.latest_packet).fromNow()})
                  </dd>
                  <dt>Duration</dt>
                  <dd><DHCPDuration duration={tx.duration_ms} /></dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Notes" />

                <DHCPNotes notes={tx.notes} />
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Transaction Steps" />


              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}