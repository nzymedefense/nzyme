import React from 'react'

function SidebarSubmenu (props) {
  let className = 'nav-link'
  let expanded = ''
  if ((window.location.pathname === '/' && props.subhref === '/') || (props.subhref !== '/' && window.location.pathname.startsWith(props.subhref))) {
    className += ' nav-link-active'
    expanded = 'show'
  }

  const id = props.subhref.replace(/[^a-zA-Z ]/g, '')

  return (
        <li className="nav-item nav-item-submenu">
            <ul className="submenu">
                <li className="nav-item-submenu">
                    <a href={'#submenu-' + id} data-bs-toggle="collapse" className={'submenu-headline ' + className}>
                        <span className="nav-icon">
                            {props.icon}
                        </span>

                        {props.title}

                        <i className="float-end fa-solid fa-angle-down" />
                    </a>

                    <ul className={'collapse nav flex-column ms-1 nav-submenu ' + expanded} id={'submenu-' + id}>
                        {props.children}
                    </ul>
                </li>
            </ul>
        </li>
  )
}

export default SidebarSubmenu
