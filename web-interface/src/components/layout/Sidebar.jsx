import React from 'react'
import ApiRoutes from '../../util/ApiRoutes'
import NavigationLink from './NavigationLink'
import SidebarSubmenu from './SidebarSubmenu'
import UserProfile from './UserProfile'

function Sidebar () {
  return (
        <div id="nav-side">
            <p className="brand">
                <a href={ApiRoutes.DASHBOARD} >nzyme</a>
            </p>

            <div className="mt-4 mb-4">
                <UserProfile />
            </div>

            <ul className="nav nav-pills flex-column mb-sm-auto mb-0 align-items-center align-items-sm-start" id="menu">
                <NavigationLink
                    href={ApiRoutes.DASHBOARD}
                    title="Dashboard"
                    icon={<i className="fa-regular fa-map" />} />

                <SidebarSubmenu title="Ethernet" subhref="/ethernet" icon={<i className="fa-solid fa-network-wired" />}>
                    <NavigationLink
                        href={ApiRoutes.ETHERNET.DNS.INDEX}
                        title="DNS"
                        icon={<i className="fa-solid fa-signs-post" />} />
                </SidebarSubmenu>

                <SidebarSubmenu title="WiFi" subhref="/dot11" icon={<i className="fa-solid fa-wifi" />}>
                    <NavigationLink
                        href={ApiRoutes.DOT11.NETWORKS.INDEX}
                        title="Networks"
                        icon={<i className="fa-solid fa-list" />} />

                    <NavigationLink
                        href={ApiRoutes.DOT11.BANDITS.INDEX}
                        title="Bandits"
                        icon={<i className="fa-solid fa-satellite-dish" />} />

                    <NavigationLink
                        href={ApiRoutes.DOT11.ASSETS.INDEX}
                        title="WiFi Assets"
                        icon={<i className="fa-solid fa-clipboard-list" />} />
                </SidebarSubmenu>

                <SidebarSubmenu title="Retrospective" subhref="/retro" icon={<i className="fa-solid fa-box-archive" />}>
                    <NavigationLink
                        href={ApiRoutes.RETRO.SEARCH.INDEX}
                        title="Search"
                        icon={<i className="fa-solid fa-magnifying-glass" />} />

                    <NavigationLink
                        href={ApiRoutes.RETRO.SERVICE_SUMMARY}
                        title="Service Summary"
                        icon={<i className="fa-solid fa-gear" />} />

                    <NavigationLink
                        href={ApiRoutes.RETRO.CONFIGURATION}
                        title="Configuration"
                        icon={<i className="fa-solid fa-wrench" />} />
                </SidebarSubmenu>

                <NavigationLink
                    href={ApiRoutes.REPORTING.INDEX}
                    title="Reporting"
                    icon={<i className="fa-solid fa-file-circle-check" />} />

                <SidebarSubmenu title="System" subhref="/system" icon={<i className="fa-solid fa-screwdriver-wrench" />}>
                    <NavigationLink
                        href={ApiRoutes.SYSTEM.CLUSTER.INDEX}
                        title="Cluster &amp; Nodes"
                        icon={<i className="fa-solid fa-circle-nodes" />} />
                    <NavigationLink
                        href={ApiRoutes.SYSTEM.TAPS.INDEX}
                        title="Taps"
                        icon={<i className="fa-solid fa-server" />} />
                    <NavigationLink
                        href={ApiRoutes.SYSTEM.AUTHENTICATION}
                        title="Authentication"
                        icon={<i className="fa-solid fa-users" />} />
                    <NavigationLink
                        href="/foo"
                        title="Database"
                        icon={<i className="fa-solid fa-database" />} />
                    <NavigationLink
                        href={ApiRoutes.SYSTEM.HEALTH.INDEX}
                        title="Health Console"
                        icon={<i className="fa-solid fa-laptop-medical" />} />
                    <NavigationLink
                        href={ApiRoutes.SYSTEM.MONITORING.INDEX}
                        title="Monitoring &amp; Metrics"
                        icon={<i className="fa-solid fa-heart-pulse" />} />
                    <NavigationLink
                        href={ApiRoutes.SYSTEM.CRYPTO.INDEX}
                        title="Keys &amp; Certificates"
                        icon={<i className="fa-solid fa-key" />} />
                    <NavigationLink
                        href={ApiRoutes.SYSTEM.VERSION}
                        title="Version"
                        icon={<i className="fa-solid fa-tag " />} />
                </SidebarSubmenu>
            </ul>
        </div>
  )
}

export default Sidebar
