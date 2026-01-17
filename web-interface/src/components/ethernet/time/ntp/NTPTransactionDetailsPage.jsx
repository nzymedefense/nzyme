import {useParams} from "react-router-dom";
import React from "react";
import ApiRoutes from "../../../../util/ApiRoutes";
import NTPTransactionDetails from "./NTPTransactionDetails";

export default function NTPTransactionDetailsPage() {

  const { transactionId } = useParams();

  return (
    <React.Fragment>
      <div className="row">
        <div className="col-md-12">
          <nav aria-label="breadcrumb">
            <ol className="breadcrumb">
              <li className="breadcrumb-item"><a href={ApiRoutes.ETHERNET.OVERVIEW}>Ethernet</a></li>
              <li className="breadcrumb-item">Time</li>
              <li className="breadcrumb-item"><a href={ApiRoutes.ETHERNET.TIME.NTP.INDEX}>NTP Transactions</a></li>
              <li className="breadcrumb-item active" aria-current="page">{transactionId}</li>
            </ol>
          </nav>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-12">
          <h1>
            NTP Transaction &quot;{transactionId}&quot;
          </h1>
        </div>
      </div>

      <NTPTransactionDetails transactionId={transactionId} />

    </React.Fragment>
  )

}