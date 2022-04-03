import React from 'react'
import ApiRoutes from "../../util/ApiRoutes";
import NavigationLink from "./NavigationLink";
import SidebarSubmenu from "./SidebarSubmenu";
import UserProfile from "./UserProfile";

function Sidebar() {

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
                    icon="fa-regular fa-map" />
                <NavigationLink
                    href="/foo/bar"
                    title="Ethernet"
                    icon="fa-solid fa-network-wired" />
                <NavigationLink
                    href="/foo/bar"
                    title="WiFi"
                    notificationCount={3}
                    icon="fa-solid fa-wifi" />
                <SidebarSubmenu subhref="/system">
                    <NavigationLink
                        href="/foo/bar"
                        title="Users"
                        icon="fa-solid fa-users" />
                    <NavigationLink
                        href="/foo/bar"
                        title="Taps"
                        icon="fa-solid fa-circle-nodes" />
                    <NavigationLink
                        href={ApiRoutes.SYSTEM.VERSION}
                        title="Version"
                        icon="fa-solid fa-tag" />
                    <NavigationLink
                        href={ApiRoutes.SYSTEM.METRICS}
                        title="Metrics"
                        icon="fa-solid fa-stethoscope" />
                </SidebarSubmenu>
            </ul>
        </div>
    );

}

export default Sidebar;