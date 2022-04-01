import React from 'react'

function SidebarSubmenu(props) {

    let className = "nav-link"
    let expanded = "";
    if ((window.location.pathname === '/' && props.subhref === '/') || (props.subhref !== '/' && window.location.pathname.startsWith(props.subhref))) {
        className += " nav-link-active";
        expanded = "show";
    }

    return (
        <li className="nav-item nav-item-submenu">
            <ul className="submenu">
                <li className="nav-item-submenu">
                    <a href="#submenu-system" data-bs-toggle="collapse" className={"submenu-headline " + className}>
                        <span className="nav-icon">
                            <i className="fa-solid fa-screwdriver-wrench fa-icon" />
                        </span>

                        System

                        <i className="float-end fa-solid fa-angle-down" />
                    </a>

                    <ul className={"collapse nav flex-column ms-1 nav-submenu " + expanded} id="submenu-system" data-bs-parent="#menu">
                        {props.children}
                    </ul>
                </li>
            </ul>
        </li>
    )

}

export default SidebarSubmenu;