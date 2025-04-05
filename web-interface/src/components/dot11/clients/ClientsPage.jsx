import React, {useContext, useEffect, useState} from "react";
import ConnectedClientsTable from "./ConnectedClientsTable";
import {TapContext} from "../../../App";
import Dot11Service from "../../../services/Dot11Service";
import ClientHistogram from "./ClientHistogram";
import {disableTapSelector, enableTapSelector} from "../../misc/TapSelector";
import {Presets} from "../../shared/timerange/TimeRange";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import {useLocation} from "react-router-dom";
import {queryParametersToFilters} from "../../shared/filtering/FilterQueryParameters";
import {CONNECTED_CLIENT_FILTER_FIELDS} from "./ConnectedClientFilterFields";
import Filters from "../../shared/filtering/Filters";
import SectionMenuBar from "../../shared/SectionMenuBar";
import ApiRoutes from "../../../util/ApiRoutes";
import {CLIENTS_MENU_ITEMS} from "./ClientsMenuItems";

const dot11Service = new Dot11Service();

const useQuery = () => {
  return new URLSearchParams(useLocation().search);
}

function ClientsPage() {

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const urlQuery = useQuery();

  const [connectedClientsHistogramTimeRange, setConnectedClientsHistogramTimeRange] = useState(Presets.RELATIVE_HOURS_24);
  const [connectedClientsHistogram, setConnectedClientsHistogram] = useState(null);
  const [connectedClients, setConnectedClients] = useState(null);
  const [connectedClientsTimeRange, setConnectedClientsTimeRange] = useState(Presets.RELATIVE_MINUTES_15);
  const [connectedClientsPage, setConnectedClientsPage] = useState(1);

  const [connectedClientsFilters, setConnectedClientsFilters] = useState(
      queryParametersToFilters(urlQuery.get("filters"), CONNECTED_CLIENT_FILTER_FIELDS)
  );

  const perPage = 25;

  useEffect(() => {
    setConnectedClientsHistogram(null);

    dot11Service.getConnectedClientsHistogram(
        connectedClientsHistogramTimeRange, selectedTaps, setConnectedClientsHistogram
    );
  }, [connectedClientsHistogramTimeRange, selectedTaps, connectedClientsFilters])

  useEffect(() => {
    setConnectedClients(null);

    dot11Service.findConnectedClients(connectedClientsTimeRange, selectedTaps, setConnectedClients,
        perPage, (connectedClientsPage-1)*perPage);
  }, [connectedClientsPage, connectedClientsTimeRange, selectedTaps, connectedClientsFilters])

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-12">
            <SectionMenuBar items={CLIENTS_MENU_ITEMS}
                            activeRoute={ApiRoutes.DOT11.CLIENTS.CONNECTED} />
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Connected Clients" slim={true}
                                       timeRange={connectedClientsHistogramTimeRange}
                                       setTimeRange={setConnectedClientsHistogramTimeRange} />

                <ClientHistogram histogram={connectedClientsHistogram} />
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Connected Clients Filters"
                                       timeRange={connectedClientsTimeRange}
                                       setTimeRange={setConnectedClientsTimeRange} />

                <Filters filters={connectedClientsFilters}
                         setFilters={setConnectedClientsFilters}
                         fields={CONNECTED_CLIENT_FILTER_FIELDS} />
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Connected Clients" />

                <p className="text-muted">
                  All clients currently observed as connected to an access point within range are listed here. The
                  advertised SSIDs of BSSIDs are compiled from the most recent three days of available data.
                </p>

                <ConnectedClientsTable clients={connectedClients}
                                       perPage={perPage}
                                       page={connectedClientsPage}
                                       setPage={setConnectedClientsPage}
                                       setFilters={setConnectedClientsFilters} />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default ClientsPage;