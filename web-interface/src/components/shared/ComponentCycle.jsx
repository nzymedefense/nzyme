import React, {useState} from "react";

export default function ComponentCycle({components}) {

  const [shownComponent, setShownComponent] = useState(components ? components[0] : null);

  const showComponent = (e, component) => {
    e.preventDefault();
    setShownComponent(component);
  }

  return (
      <div className="row">
        <div className="col-md-2">
          <div className="list-group list-group-flush">
          {components.map((component, i) => {
            return (
                <a key={i} href="#"  className={"list-group-item list-group-item-action " + (shownComponent.name === component.name ? "active" : null)} onClick={(e) => showComponent(e, component)}>
                  {component.name}
                </a>
            )
          })}
          </div>
        </div>
        <div className="col-md-10">
          {shownComponent.element}
        </div>
      </div>
  )


}