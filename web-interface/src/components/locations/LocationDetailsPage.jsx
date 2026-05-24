import React, {useEffect, useState} from "react";
import usePageTitle from "../../util/UsePageTitle";
import LoadingSpinner from "../misc/LoadingSpinner";
import {useParams} from "react-router-dom";
import useSelectedTenant from "../system/tenantselector/useSelectedTenant";
import LocationsService from "../../services/LocationsService";
import ApiRoutes from "../../util/ApiRoutes";
import LocationWeatherCondition from "./shared/LocationWeatherCondition";
import CardTitleWithControls from "../shared/CardTitleWithControls";
import LocationTemperature from "./shared/LocationTemperature";
import LocationWind from "./shared/LocationWind";
import LocationVisibility from "./shared/LocationVisibility";
import LatLonMap from "../shared/LatLonMap";
import EnvironmentalAlertsList from "./EnvironmentalAlertsList";
import LatitudeLongitude from "../shared/LatitudeLongitude";

const locationsService = new LocationsService();

export default function LocationDetailsPage() {

  const {uuid} = useParams();

  const [organizationId, tenantId] = useSelectedTenant();

  const [location, setLocation] = useState(null);

  usePageTitle(location ? `Location: ${location.name}` : "Location Details");

  useEffect(() => {
    locationsService.findOne(uuid, organizationId, tenantId, setLocation);
  }, [uuid, organizationId, tenantId]);

  if (!location) {
    return <LoadingSpinner />;
  }

  /*
  taps
  alerts
  floors
  current weather
  metar
  severe environmental alerts
  map
  description
  local time
   */

  return (
    <>
      <div className="row">
        <div className="col-md-9">
          <nav aria-label="breadcrumb">
            <ol className="breadcrumb">
              <li className="breadcrumb-item"><a href={ApiRoutes.LOCATIONS.INDEX}>Locations</a></li>
              <li className="breadcrumb-item active" aria-current="page">{location.name}</li>
            </ol>
          </nav>
        </div>
        <div className="col-md-3">
              <span className="float-end">
                <a className="btn btn-secondary" href={ApiRoutes.LOCATIONS.INDEX}>Back</a>
              </span>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-12">
          <h1>Location &quot;{location.name}&quot;</h1>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-12">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Coordinates" slim={true}/>

              { location.latitude && location.longitude ?
                <><LatLonMap editMode={false}
                           containerHeight={400}
                           defaultZoomLevel={10}
                           latitude={location.latitude}
                           longitude={location.longitude} />
                  <dl className="mt-3 mb-0">
                    <dt>Latitude, Longitude</dt>
                    <dd>
                      <LatitudeLongitude latitude={location.latitude}
                                         longitude={location.longitude}
                                         skipAccuracy={true} />
                    </dd>
                  </dl>
                </> : <span className="text-muted">No coordinates defined for this location.</span>}
            </div>
          </div>
        </div>
      </div>

      <div className="row mt-3">
        <div className="col-md-4">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Current Weather Observation" slim={true}/>

              <dl className="mb-0 mt-2">
                <dt>Condition</dt>
                <dd><LocationWeatherCondition environment={location.environment} /></dd>
                <dt>Temperature</dt>
                <dd><LocationTemperature environment={location.environment} /></dd>
                <dt>Wind</dt>
                <dd><LocationWind environment={location.environment} /></dd>
                <dt>Visibility</dt>
                <dd><LocationVisibility environment={location.environment} /></dd>
              </dl>
            </div>
          </div>
        </div>
        <div className="col-md-8">
          <div className="card">
            <div className="card-body">
              <CardTitleWithControls title="Environmental Alerts" slim={true}/>

              <div style={{maxHeight: 195, overflowY: "auto"}}>
                <EnvironmentalAlertsList environment={location.environment} />
              </div>
            </div>
          </div>
        </div>
      </div>
    </>

  )

}