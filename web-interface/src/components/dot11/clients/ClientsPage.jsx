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

  const [histogramTimeRange, sethistogramTimeRange] = useState(Presets.RELATIVE_HOURS_24);
  const [histogram, setHistogram] = useState(null);
  const [clients, setClients] = useState(null);
  const [clientsTimeRange, setClientsTimeRange] = useState(Presets.RELATIVE_MINUTES_15);
  const [page, setPage] = useState(1);

  const [filters, setFilters] = useState(
      queryParametersToFilters(urlQuery.get("filters"), CONNECTED_CLIENT_FILTER_FIELDS)
  );

  const perPage = 25;

  useEffect(() => {
    setHistogram(null);

    dot11Service.getConnectedClientsHistogram(
        histogramTimeRange,
        filters,
        selectedTaps,
        setHistogram
    );
  }, [histogramTimeRange, selectedTaps, filters])

  useEffect(() => {
    setClients(null);

    dot11Service.findConnectedClients(
        clientsTimeRange,
        filters,
        selectedTaps,
        setClients,
        perPage,
        (page-1)*perPage
    );
  }, [page, clientsTimeRange, selectedTaps, filters])

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
                                       timeRange={histogramTimeRange}
                                       setTimeRange={sethistogramTimeRange} />

                <ClientHistogram histogram={histogram} />
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Connected Clients Filters"
                                       timeRange={clientsTimeRange}
                                       setTimeRange={setClientsTimeRange} />

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
                <CardTitleWithControls title="Connected Clients" />

                <p className="text-muted">
                  All clients currently observed as connected to an access point within range are listed here. The
                  advertised SSIDs of BSSIDs are compiled from the most recent three days of available data.
                </p>

                <ConnectedClientsTable clients={clients}
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