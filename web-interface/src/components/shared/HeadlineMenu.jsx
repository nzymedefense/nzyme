import React from 'react';

export default function HeadlineMenu(props) {

  const headline = props.headline;
  const items = props.items;
  const activeRoute = props.activeRoute;

  return (
      <div className="btn-group dropend dropdown-headline">
        <button type="button" className="btn btn-lg btn-light dropdown-toggle" data-bs-toggle="dropdown">
          <span style={{marginRight: 5}}>{headline}</span>
        </button>
        <ul className="dropdown-menu">
          {items.map((item, i) => {
            return (
                <li key={i}>
                  <a className={"dropdown-item " + (item.href === activeRoute ? "active" : null)} href={item.href}>
                    {item.name}
                  </a>
                </li>
            )
          })}
        </ul>
      </div>
  )

}