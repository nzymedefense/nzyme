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
import Dot11MacAddress from "../../shared/context/macs/Dot11MacAddress";
import MacAddressContextLine from "../../shared/context/macs/details/MacAddressContextLine";
import TapBasedSignalStrengthTable from "../../shared/TapBasedSignalStrengthTable";
import BSSIDSignalWaterfallChart from "./BSSIDSignalWaterfallChart";
import ReadOnlyTrilaterationResultFloorPlanWrapper
  from "../../shared/floorplan/ReadOnlyTrilaterationResultFloorPlanWrapper";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import {Presets, Relative} from "../../shared/timerange/TimeRange";
import ChannelSelector from "../util/ChannelSelector";

const dot11Service = new Dot11Service();

function BSSIDDetailsPage() {

  const {bssidParam} = useParams();

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [bssid, setBSSID] = useState(null);
  const [ssids, setSSIDs] = useState(null);

  const [ssidsTimeRange, setSsidsTimeRange] = useState(Presets.RELATIVE_HOURS_24);
  const [signalWaterfallTimeRange, setSignalWaterfallTimeRange] = useState(Presets.RELATIVE_HOURS_24);
  const [advertisementsBeaconTimeRange, setAdvertisementsBeaconTimeRange] = useState(Presets.RELATIVE_HOURS_24);
  const [advertisementsProbeRespTimeRange, setAdvertisementsProbeRespTimeRange] = useState(Presets.RELATIVE_HOURS_24);
  const [activeChannelsTimeRange, setActiveChannelsTimeRange] = useState(Presets.RELATIVE_HOURS_24);
  const [discoFramesTimeRange, setDiscoFramesTimeRange] = useState(Presets.RELATIVE_HOURS_24);
  const [discoPairsTimeRange, setDiscoPairsTimeRange] = useState(Presets.RELATIVE_HOURS_24);

  const [waterfallFrequency, setWaterfallFrequency] = useState(0);

  const [trilaterationTimeRange, setTrilaterationTimeRange] = useState(Presets.RELATIVE_MINUTES_15)
  const [trilaterationFloor, setTrilaterationFloor] = useState(null);
  const [trilaterationResult, setTrilaterationResult] = useState(null);
  const [trilaterationError, setTrilaterationError] = useState(null);
  const [trilaterationRevision, setTrilaterationRevision] = useState(0);

  useEffect(() => {
    setBSSID(null);
    dot11Service.findBSSID(bssidParam, selectedTaps, setBSSID);
  }, [bssidParam, selectedTaps]);

  useEffect(() => {
    setSSIDs(null);
    dot11Service.findSSIDsOfBSSID(bssidParam, ssidsTimeRange, selectedTaps, (ssids) => setSSIDs(ssids));
  }, [ssidsTimeRange, selectedTaps]);

  useEffect(() => {
    setTrilaterationResult(null);
    if (trilaterationFloor == null) {
      dot11Service.findBSSIDLocation(
          bssidParam, null, null, trilaterationTimeRange, setTrilaterationResult, setTrilaterationError
      );
    } else {
      dot11Service.findBSSIDLocation(
          bssidParam, trilaterationFloor.location, trilaterationFloor.floor, trilaterationTimeRange, setTrilaterationResult, setTrilaterationError
      );
    }
  }, [bssidParam, selectedTaps, trilaterationFloor, trilaterationRevision, trilaterationTimeRange]);

  useEffect(() => {
    if (bssid) {
      setWaterfallFrequency(bssid.frequencies[0]);
    }
  }, [bssid]);

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

  const ssidsTable = () => {
    if (!ssids || !bssid) {
      return <LoadingSpinner />
    }

    return (
        <table style={{width: "100%"}}>
          <tbody>
          <BSSIDDetailsRows bssid={bssid.summary.bssid} ssids={ssids} loading={false} hideBSSIDLink={true}/>
          </tbody>
        </table>
    )
  }

  const onFloorSelected = (locationUuid, floorUuid) => {
    setTrilaterationFloor({location: locationUuid, floor: floorUuid});
  }

  const onTrilaterationRefresh = () => {
    setTrilaterationRevision((prevRev => prevRev + 1));
  }

  if (!bssid) {
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
              BSSID &quot;{bssid.summary.bssid.address} ({bssid.summary.bssid.oui ? bssid.summary.bssid.oui : "Unknown Vendor"})&quot;
            </h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Access Point Information"
                                       fixedAppliedTimeRange={Presets.ALL_TIME} />

                <dl className="mb-0">
                  <dt>MAC Address</dt>
                  <dd>
                    <Dot11MacAddress addressWithContext={bssid.summary.bssid} />
                  </dd>
                  <dt>Vendor</dt>
                  <dd>
                    {bssid.summary.bssid.oui ? bssid.summary.bssid.oui : "Unknown"}
                  </dd>
                  <dt>Name &amp; Description (from Context)</dt>
                  <dd>
                    <MacAddressContextLine address={bssid.summary.bssid.address} context={bssid.summary.bssid.context} />
                  </dd>
                </dl>
              </div>
            </div>
          </div>

          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Activity"
                                       fixedAppliedTimeRange={Presets.ALL_TIME} />

                <dl className="mb-0">
                  <dt>First Seen</dt>
                  <dd>
                    {moment(bssid.summary.first_seen).format()}{' '}
                    <span className="text-muted">(Note: This value is affected by data retention times.)</span>
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
                <CardTitleWithControls title="Advertised SSIDs"
                                       timeRange={ssidsTimeRange}
                                       setTimeRange={setSsidsTimeRange} />

                {ssidsTable()}
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
                    <CardTitleWithControls title="Fingerprints"
                                           fixedAppliedTimeRange={Presets.ALL_TIME} />

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
                    <CardTitleWithControls title="Average Signal Strength"
                                           smallText="All Channels"
                                           fixedAppliedTimeRange={Presets.RELATIVE_MINUTES_15} />

                    <TapBasedSignalStrengthTable strengths={bssid.signal_strength}/>
                  </div>
                </div>
              </div>
            </div>

            <div className="row mt-3">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <CardTitleWithControls title="Signal Waterfall"
                                           timeRange={signalWaterfallTimeRange}
                                           setTimeRange={setSignalWaterfallTimeRange} />

                    <ChannelSelector currentFrequency={waterfallFrequency}
                                     setFrequency={setWaterfallFrequency}
                                     frequencies={bssid.frequencies}/>

                    <BSSIDSignalWaterfallChart bssid={bssid.summary.bssid.address}
                                               frequency={waterfallFrequency}
                                               timeRange={signalWaterfallTimeRange} />
                  </div>
                </div>
              </div>
            </div>

            <div className="row mt-3">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <CardTitleWithControls title="Physical Location / Trilateration"
                                           helpLink="https://go.nzyme.org/wifi-trilateration"
                                           timeRange={trilaterationTimeRange}
                                           setTimeRange={setTrilaterationTimeRange} />

                    <ReadOnlyTrilaterationResultFloorPlanWrapper data={trilaterationResult}
                                                                 onFloorSelected={onFloorSelected}
                                                                 taps={selectedTaps}
                                                                 onRefresh={onTrilaterationRefresh}
                                                                 error={trilaterationError} />
                  </div>
                </div>
              </div>
            </div>

            <div className="row mt-3">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <CardTitleWithControls title="Beacon Advertisements"
                                           timeRange={advertisementsBeaconTimeRange}
                                           setTimeRange={setAdvertisementsBeaconTimeRange} />

                    <BSSIDAdvertisementHistogram bssid={bssid.summary.bssid.address}
                                                 timeRange={advertisementsBeaconTimeRange}
                                                 setTimeRange={setAdvertisementsBeaconTimeRange}
                                                 parameter="beacon_count" />
                  </div>
                </div>
              </div>
            </div>

            <div className="row mt-3">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <CardTitleWithControls title="Probe Response Advertisements"
                                           timeRange={advertisementsProbeRespTimeRange}
                                           setTimeRange={setAdvertisementsProbeRespTimeRange} />

                    <BSSIDAdvertisementHistogram bssid={bssid.summary.bssid.address}
                                                 timeRange={advertisementsProbeRespTimeRange}
                                                 setTimeRange={setAdvertisementsProbeRespTimeRange}
                                                 parameter="proberesp_count"/>
                  </div>
                </div>
              </div>
            </div>

            <div className="row mt-3">
              <div className="col-md-12">
                <div className="card">
                  <div className="card-body">
                    <CardTitleWithControls title="Active Channels"
                                           timeRange={activeChannelsTimeRange}
                                           setTimeRange={setActiveChannelsTimeRange} />

                    <BSSIDChannelUsageHistogram bssid={bssid.summary.bssid.address}
                                                timeRange={activeChannelsTimeRange} />
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
                    <CardTitleWithControls title="Clients connected to BSSID"
                                           fixedAppliedTimeRange={Presets.RELATIVE_HOURS_24} />

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
                <CardTitleWithControls title="Disconnection Frames"
                                       timeRange={discoFramesTimeRange}
                                       setTimeRange={setDiscoFramesTimeRange} />

                <DiscoHistogram discoType="disconnection"
                                timeRange={discoFramesTimeRange}
                                setTimeRange={setDiscoFramesTimeRange}
                                bssids={[bssid.summary.bssid.address]} />

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
                <CardTitleWithControls title="Top Disconnection Pairs"
                                       timeRange={discoPairsTimeRange}
                                       setTimeRange={setDiscoPairsTimeRange} />

                <DiscoPairsTable bssids={[bssid.summary.bssid.address]}
                                 timeRange={discoPairsTimeRange}
                                 highlightValue={bssid.summary.bssid.address} />

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