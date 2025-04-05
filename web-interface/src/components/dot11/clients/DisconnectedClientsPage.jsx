import React, {useContext, useEffect, useState} from "react";
import {TapContext} from "../../../App";
import Dot11Service from "../../../services/Dot11Service";
import DisconnectedClientsTable from "./DisconnectedClientsTable";
import ClientHistogram from "./ClientHistogram";
import {disableTapSelector, enableTapSelector} from "../../misc/TapSelector";
import {Presets} from "../../shared/timerange/TimeRange";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import HelpBubble from "../../misc/HelpBubble";
import {useLocation} from "react-router-dom";
import {queryParametersToFilters} from "../../shared/filtering/FilterQueryParameters";
import {CONNECTED_CLIENT_FILTER_FIELDS} from "./ConnectedClientFilterFields";
import Filters from "../../shared/filtering/Filters";
import {DISCONNECTED_CLIENT_FILTER_FIELDS} from "./DisconnectedClientFilterFields";
import SectionMenuBar from "../../shared/SectionMenuBar";
import {CLIENTS_MENU_ITEMS} from "./ClientsMenuItems";
import ApiRoutes from "../../../util/ApiRoutes";

const dot11Service = new Dot11Service();

const useQuery = () => {
  return new URLSearchParams(useLocation().search);
}

function ClientsPage() {

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const urlQuery = useQuery();

  const [histogramTimeRange, setHistogramTimeRange] = useState(Presets.RELATIVE_HOURS_24);
  const [histogram, setHistogram] = useState(null);

  const [clients, setClients] = useState(null);
  const [timeRange, setTimeRange] = useState(Presets.RELATIVE_MINUTES_15);
  const [page, setPage] = useState(1);

  const [skipRandomized, setSkipRandomized] = useState(true);

  const [filters, setFilters] = useState(
      queryParametersToFilters(urlQuery.get("filters"), DISCONNECTED_CLIENT_FILTER_FIELDS)
  );

  const perPage = 25;

  useEffect(() => {
    setHistogram(null);

    dot11Service.getDisconnectedClientsHistogram(
        histogramTimeRange, filters, skipRandomized, selectedTaps, setHistogram
    );
  }, [histogramTimeRange, skipRandomized, selectedTaps, filters])

  useEffect(() => {
    setClients(null);

    dot11Service.findDisconnectedClients(
        timeRange,
        filters,
        skipRandomized,
        selectedTaps,
        setClients,
        perPage,
        (page-1)*perPage
    );
  }, [page, timeRange, skipRandomized, selectedTaps, filters])

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

  const disconnectedTitle = () => {
    if (skipRandomized) {
      return "Disconnected Clients (Excluding Randomized)"
    } else {
      return "Disconnected Clients"
    }
  }

  const onDisconnectedClientsSkipRandomizedChange = () => {
    setSkipRandomized((!skipRandomized));
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-12">
            <SectionMenuBar items={CLIENTS_MENU_ITEMS}
                            activeRoute={ApiRoutes.DOT11.CLIENTS.DISCONNECTED} />
          </div>
        </div>


        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title={disconnectedTitle()} slim={true}
                                       timeRange={histogramTimeRange}
                                       setTimeRange={setHistogramTimeRange} />

                <ClientHistogram histogram={histogram} />
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title={disconnectedTitle() + " Filters"}
                                       timeRange={timeRange}
                                       setTimeRange={setTimeRange} />

                <Filters filters={filters}
                         setFilters={setFilters}
                         fields={CONNECTED_CLIENT_FILTER_FIELDS} />
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title={disconnectedTitle()} />

                <p className="text-muted">
                  <em>Disconnected</em> clients are currently not connected to any WiFi network but still making their
                  presence known by transmitting probe request frames.
                </p>

                <p className="text-muted">
                  It should be noted that many modern WiFi devices utilize MAC address randomization when they are not
                  connected to a network. This practice improves privacy by complicating tracking efforts, resulting in
                  a wide range of distinct MAC addresses being captured by nzyme. The advertised SSIDs of BSSIDs are
                  compiled from the most recent three days of available data.{' '}

                  <HelpBubble link="https://go.nzyme.org/kb-mac-randomization" />
                </p>

                <div className="form-check form-switch mb-3">
                  <input className="form-check-input"
                         type="checkbox"
                         role="switch"
                         id="skipRandomized"
                         onChange={onDisconnectedClientsSkipRandomizedChange}
                         checked={skipRandomized}/>
                  <label className="form-check-label"
                         htmlFor="skipRandomized">
                    Exclude Clients with Randomized MAC Address
                  </label>
                </div>

                <DisconnectedClientsTable clients={clients}
                                          perPage={perPage}
                                          page={page}
                                          setPage={setPage}
                                          setFilters={setFilters} />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default ClientsPage;