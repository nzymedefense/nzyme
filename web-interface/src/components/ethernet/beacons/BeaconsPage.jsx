import React, {useContext, useEffect, useState} from "react";
import {useLocation} from "react-router-dom";
import numeral from "numeral";
import SectionMenuBar from "../../shared/SectionMenuBar";
import ApiRoutes from "../../../util/ApiRoutes";
import usePageTitle from "../../../util/UsePageTitle";
import useSelectedTenant from "../../system/tenantselector/useSelectedTenant";
import Filters from "../../shared/filtering/Filters";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import {queryParametersToFilters} from "../../shared/filtering/FilterQueryParameters";
import BeaconsService from "../../../services/ethernet/BeaconsService";
import {BEACONS_MENU_ITEMS} from "./BeaconsMenuItems";
import {BEACONS_FILTER_FIELDS} from "./BeaconsFilterFields";
import BeaconsTable from "./BeaconsTable";
import {beaconClassificationToClassName, beaconClassificationToName} from "./BeaconClassification";
import {Presets} from "../../shared/timerange/TimeRange";
import {UserContext} from "../../../App";
import {userHasPermission} from "../../../util/Tools";
import ActiveBeaconsHistogram from "./ActiveBeaconsHistogram";
import BulkBeaconClassification from "./BulkBeaconClassification";

const beaconsService = new BeaconsService();

const useQuery = () => {
  return new URLSearchParams(useLocation().search);
}

export default function BeaconsPage() {

  usePageTitle("Ethernet Beacons");

  const [organizationId, tenantId] = useSelectedTenant();
  const user = useContext(UserContext);
  const canEdit = userHasPermission(user, "ethernet_monitoring_manage");
  const urlQuery = useQuery();

  const [filters, setFilters] = useState(
    queryParametersToFilters(urlQuery.get("filters"), BEACONS_FILTER_FIELDS)
  );
  const [orderColumn, setOrderColumn] = useState("last_seen");
  const [orderDirection, setOrderDirection] = useState("DESC");

  const [beacons, setBeacons] = useState(null);
  const [page, setPage] = useState(1);
  const [revision, setRevision] = useState(new Date());

  const [histogramTimeRange, setHistogramTimeRange] = useState(Presets.RELATIVE_HOURS_24);
  const [histogram, setHistogram] = useState(null);

  const perPage = 25;

  const applyFilters = (newFilters) => {
    setPage(1);
    setFilters(newFilters);
  }

  const applyOrderColumn = (column) => {
    setPage(1);
    setOrderColumn(column);
  }

  const applyOrderDirection = (direction) => {
    setPage(1);
    setOrderDirection(direction);
  }

  useEffect(() => {
    setBeacons(null);
    beaconsService.findAllBeacons(
      organizationId, tenantId, filters, orderColumn, orderDirection, perPage, (page-1)*perPage, setBeacons
    );
  }, [page, filters, orderColumn, orderDirection, organizationId, tenantId, revision]);

  useEffect(() => {
    setHistogram(null);
    beaconsService.findActiveBeaconsHistogram(organizationId, tenantId, filters, histogramTimeRange, setHistogram);
  }, [organizationId, tenantId, filters, histogramTimeRange, revision]);

  const classificationCounts = () => {
    if (!beacons) {
      return null;
    }

    return (
      <p className="mb-0">
        <strong>Total:</strong> {numeral(beacons.total).format("0,0")}
        {["UNKNOWN", "BENIGN", "SUSPICIOUS", "MALICIOUS"].map((c) => {
          return (
            <span key={c} className="ms-3">
              <span className={"classification " + beaconClassificationToClassName(c)}>{beaconClassificationToName(c)}:</span>{' '}
              {numeral(beacons.classification_counts[c] || 0).format("0,0")}
            </span>
          )
        })}
      </p>
    )
  }

  return (
    <React.Fragment>
      <div className="row">
        <div className="col-md-10">
          <SectionMenuBar items={BEACONS_MENU_ITEMS}
                          activeRoute={ApiRoutes.ETHERNET.BEACONS.INDEX}/>
        </div>

        <div className="col-md-2">
          <a href="https://go.nzyme.org/ethernet-beacons" className="btn btn-secondary float-end">Help</a>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-12">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Filters"
                                     timeRange={histogramTimeRange}
                                     setTimeRange={setHistogramTimeRange}
                                     doNotPersistTimeRange={true} />

              <Filters filters={filters}
                       setFilters={applyFilters}
                       fields={BEACONS_FILTER_FIELDS}
                       onMonitorsReady={() => {}} />
            </div>
          </div>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-12">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Active Beacons"
                                     refreshAction={() => setRevision(new Date())} />

              <ActiveBeaconsHistogram histogram={histogram}
                                      timeRange={histogramTimeRange}
                                      setTimeRange={setHistogramTimeRange} />
            </div>
          </div>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-12">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Beacons"
                                     refreshAction={() => setRevision(new Date())} />

              <p className="text-muted">
                Destinations that devices in your network call out to in regular intervals.
              </p>

              <div className="row mb-3 align-items-center">
                <div className="col-md-7">
                  {classificationCounts()}
                </div>
                <div className="col-md-5 text-end">
                  {canEdit && beacons ?
                    <BulkBeaconClassification total={beacons.total}
                                              filters={filters}
                                              organizationId={organizationId}
                                              tenantId={tenantId}
                                              onChanged={() => setRevision(new Date())} />
                    : null}
                </div>
              </div>

              <BeaconsTable beacons={beacons}
                            page={page}
                            setPage={setPage}
                            perPage={perPage}
                            orderColumn={orderColumn}
                            setOrderColumn={applyOrderColumn}
                            orderDirection={orderDirection}
                            setOrderDirection={applyOrderDirection}
                            filterFields={BEACONS_FILTER_FIELDS}
                            setFilters={applyFilters}
                            organizationId={organizationId}
                            tenantId={tenantId}
                            canEdit={canEdit}
                            onBeaconChanged={() => setRevision(new Date())} />
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )

}
