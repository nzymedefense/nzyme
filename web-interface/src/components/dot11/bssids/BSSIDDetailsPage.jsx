import React, {useContext, useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import ApiRoutes from "../../../util/ApiRoutes";
import Dot11Service from "../../../services/Dot11Service";
import {TapContext} from "../../../App";
import LoadingSpinner from "../../misc/LoadingSpinner";
import moment from "moment/moment";
import BSSIDDetailsRows from "./BSSIDDetailsRows";
import SSIDAccessPointClients from "./ssids/SSIDAccessPointClients";
import BSSIDAdvertisementHistogram from "./BSSIDAdvertisementHistogram";
import BSSIDChannelUsageHistogram from "./BSSIDChannelUsageHistogram";
import DiscoPairsTable from "../disco/DiscoPairsTable";
import DiscoHistogram from "../disco/DiscoHistogram";
import {disableTapSelector, enableTapSelector} from "../../misc/TapSelector";

const dot11Service = new Dot11Service();

function BSSIDDetailsPage() {

  const {bssidParam} = useParams();

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [bssid, setBSSID] = useState(null);
  const [ssids, setSSIDs] = useState(null);

  useEffect(() => {
    dot11Service.findBSSID(bssidParam, selectedTaps, setBSSID);
    dot11Service.findSSIDsOfBSSID(bssidParam, 24*60, selectedTaps,
        (ssids) => setSSIDs(ssids)
    )
  }, [bssidParam, selectedTaps]);

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

  if (!bssid || ssids == null) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-12">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.OVERVIEW}>WiFi</a></li>
                <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.NETWORKS.BSSIDS}>Access Points</a></li>
                <li className="breadcrumb-item">{bssid.summary.bssid.address}</li>
                <li className="breadcrumb-item active" aria-current="page">Details</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-12">
            <h1>
              BSSID &quot;{bssid.summary.bssid.address} ({bssid.summary.oui ? bssid.summary.oui : "Unknown Vendor"})&quot;
            </h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Access Point Information <small>All Time</small></h3>

                <dl className="mb-0">
                  <dt>MAC Address</dt>
                  <dd>
                    <span className="dot11-mac">{bssid.summary.bssid.address}</span>
                  </dd>
                  <dt>Vendor</dt>
                  <dd>
                    {bssid.summary.oui ? bssid.summary.oui : "Unknown"}
                  </dd>
                </dl>
              </div>
            </div>
          </div>

          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Activity <small>All Time</small></h3>

                <dl className="mb-0">
                  <dt>First Seen</dt>
                  <dd>
                    {moment(bssid.summary.first_seen).format()}{' '}
                    (Note: 802.11/WiFi data retention time is {bssid.data_retention_days} days)
                  </dd>
                  <dt>Last Seen</dt>
                  <dd>{moment(bssid.summary.last_seen).format()}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <h3>Advertised SSIDs <small>Last 24 Hours</small></h3>

                <table style={{width: "100%"}}>
                  <tbody>
                  <BSSIDDetailsRows bssid={bssid.summary} ssids={ssids} loading={false} hideBSSIDLink={true} />
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-8">
            <div className="row">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <h3>Fingerprints <small>All Time, All SSIDs</small></h3>

                    <ul className="mb-0">
                      {bssid.summary.fingerprints.map((fp, i) => {
                        return <li key={i}>{fp}</li>
                      })}
                    </ul>
                  </div>
                </div>
              </div>
            </div>

            <div className="row mt-3">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <h3>Beacon Advertisements <small>Last 24 Hours, All SSIDs</small></h3>

                    <BSSIDAdvertisementHistogram bssid={bssid.summary.bssid.address} parameter="beacon_count" />
                  </div>
                </div>
              </div>
            </div>

            <div className="row mt-3">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <h3>Probe Response Advertisements <small>Last 24 Hours, All SSIDs</small></h3>

                    <BSSIDAdvertisementHistogram bssid={bssid.summary.bssid.address} parameter="proberesp_count" />
                  </div>
                </div>
              </div>
            </div>

            <div className="row mt-3">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <h3>Active Channels <small>Last 24 Hours, All SSIDs</small></h3>

                    <BSSIDChannelUsageHistogram bssid={bssid.summary.bssid.address} minutes={24*60} />
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="col-md-4">
            <div className="row">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <h3>
                      Clients connected to BSSID <small>Last 24 Hours</small>
                    </h3>

                    <p className="text-muted">
                      Please note that recording clients generally demands closer proximity, as
                      well as more precise matching of transmission-related parameters compared to recording access
                      points. Additionally, many modern devices will randomize their MAC addresses and hide vendor
                      information.
                    </p>

                    <SSIDAccessPointClients clients={bssid.clients} />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <h3>Disconnection Frames <small>Last 24 hours</small></h3>

                <DiscoHistogram discoType="disconnection" minutes={24*60} bssids={[bssid.summary.bssid.address]} />

                <p className="text-muted mb-0 mt-3">
                  Disconnection activity refers to the sum of deauthentication and disassociation frames.
                </p>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <h3 className="mb-0">Top Disconnection Pairs <small>Last 24 hours</small></h3>

                <DiscoPairsTable bssids={[bssid.summary.bssid.address]} highlightValue={bssid.summary.bssid.address} />

                <p className="mb-0 mt-3 text-muted">
                  The MAC address of this access point is <span className="highlighted">highlighted.</span>
                </p>
              </div>
            </div>
          </div>
        </div>

      </React.Fragment>
  )

}

export default BSSIDDetailsPage;