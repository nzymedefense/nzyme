import React, {useContext} from 'react'
import ApiRoutes from '../../util/ApiRoutes'
import NavigationLink from './NavigationLink'
import SidebarSubmenu from './SidebarSubmenu'
import UserProfileBlock from "./UserProfileBlock";

import {UserContext} from "../../App";

function Sidebar() {

  const user = useContext(UserContext);

  return (
  <div id="nav-side">
    <p className="brand">
      <a href={ApiRoutes.DASHBOARD} >nzyme</a>
    </p>

    <div className="mt-4 mb-4">
      <UserProfileBlock user={user} />
    </div>

    <ul className="nav nav-pills flex-column mb-sm-auto mb-0 align-items-center align-items-sm-start" id="menu">
      <NavigationLink
          href={ApiRoutes.DASHBOARD}
          title="Dashboard"
          icon={<i className="sidebar-icon fa-regular fa-map" />} />

      <SidebarSubmenu title="Ethernet"
                      subhref="/ethernet"
                      icon={<i className="sidebar-icon fa-solid fa-network-wired" />}
                      show={true}>
        <NavigationLink
            href={ApiRoutes.ETHERNET.DNS.INDEX}
            title="DNS"
            icon={<i className="sidebar-icon fa-solid fa-signs-post" />} />
        <NavigationLink
            href="#"
            title="Beacons"
            icon={<i className="sidebar-icon fa-solid fa-location-arrow" />} />
      </SidebarSubmenu>

      <SidebarSubmenu title="WiFi"
                      subhref="/dot11"
                      icon={<i className="sidebar-icon fa-solid fa-wifi" />}
                      show={true}>
        <NavigationLink
            href={ApiRoutes.DOT11.NETWORKS.BSSIDS}
            title="Access Points"
            icon={<i className="sidebar-icon fa-solid fa-list" />} />

        <NavigationLink
            href={ApiRoutes.DOT11.BANDITS.INDEX}
            title="Bandits"
            icon={<i className="sidebar-icon fa-solid fa-satellite-dish" />} />

        <NavigationLink
            href={ApiRoutes.DOT11.ASSETS.INDEX}
            title="WiFi Assets"
            icon={<i className="sidebar-icon fa-solid fa-clipboard-list" />} />
      </SidebarSubmenu>

      <SidebarSubmenu title="Retrospective"
                      subhref="/retro"
                      icon={<i className="sidebar-icon fa-solid fa-box-archive" />}
                      show={user.is_orgadmin
                          || user.is_superadmin
                          || user.feature_permissions.includes("retrospective_view") }>
        <NavigationLink
            href={ApiRoutes.RETRO.SEARCH.INDEX}
            title="Search"
            icon={<i className="sidebar-icon fa-solid fa-magnifying-glass" />} />

        <NavigationLink
            href={ApiRoutes.RETRO.SERVICE_SUMMARY}
            title="Service Summary"
            icon={<i className="sidebar-icon fa-solid fa-gear" />} />

        <NavigationLink
            href={ApiRoutes.RETRO.CONFIGURATION}
            title="Configuration"
            icon={<i className="sidebar-icon fa-solid fa-wrench" />} />
      </SidebarSubmenu>

      <SidebarSubmenu title="Reporting"
                      subhref="/reporting"
                      icon={<i className="sidebar-icon fa-solid fa-file-circle-check" />}
                      show={user.is_orgadmin
                          || user.is_superadmin
                          || user.feature_permissions.includes("reports_manage")
                          || user.feature_permissions.includes("reports_view")}>
        <NavigationLink
            href={ApiRoutes.REPORTING.INDEX}
            title="Reporting"
            icon={<i className="sidebar-icon fa-solid fa-file-circle-check" />} />
      </SidebarSubmenu>

      <SidebarSubmenu title="System"
                      subhref="/system"
                      icon={<i className="sidebar-icon fa-solid fa-screwdriver-wrench"/>}
                      show={user.is_orgadmin || user.is_superadmin } >
        <NavigationLink
            show={user.is_superadmin}
            href={ApiRoutes.SYSTEM.CLUSTER.INDEX}
            title="Cluster &amp; Nodes"
            icon={<i className="sidebar-icon fa-solid fa-circle-nodes" />} />
        <NavigationLink
            href={ApiRoutes.SYSTEM.TAPS.INDEX}
            title="Taps"
            icon={<i className="sidebar-icon fa-solid fa-server" />} />
        <NavigationLink
            show={user.is_superadmin} 
            href={ApiRoutes.SYSTEM.EVENTS.INDEX}
            title="Events &amp; Actions"
            icon={<i className="sidebar-icon fa-solid fa-bolt" />} />
        <NavigationLink
            href={ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.INDEX}
            title={user.is_superadmin ? "Authentication" : "Organization" }
            icon={<i className="sidebar-icon fa-solid fa-users" />} />
        <NavigationLink
            show={user.is_superadmin}
            href="/foo"
            title="Database"
            icon={<i className="sidebar-icon fa-solid fa-database" />} />
        <NavigationLink
            show={user.is_superadmin}
            href={ApiRoutes.SYSTEM.INTEGRATIONS.INDEX}
            title="Integrations"
            icon={<i className="sidebar-icon fa-solid fa-puzzle-piece" />} />
        <NavigationLink
            show={user.is_superadmin}
            href={ApiRoutes.SYSTEM.HEALTH.INDEX}
            title="Health Console"
            icon={<i className="sidebar-icon fa-solid fa-laptop-medical" />} />
        <NavigationLink
            show={user.is_superadmin}
            href={ApiRoutes.SYSTEM.MONITORING.INDEX}
            title="Monitoring &amp; Metrics"
            icon={<i className="sidebar-icon fa-solid fa-heart-pulse" />} />
        <NavigationLink
            show={user.is_superadmin}
            href={ApiRoutes.SYSTEM.CRYPTO.INDEX}
            title="Keys &amp; Certificates"
            icon={<i className="sidebar-icon fa-solid fa-key" />} />
        <NavigationLink
            show={user.is_superadmin}
            href={ApiRoutes.SYSTEM.VERSION}
            title="Version"
            icon={<i className="sidebar-icon fa-solid fa-tag " />} />
      </SidebarSubmenu>
    </ul>
  </div>
  )
}

export default Sidebar
