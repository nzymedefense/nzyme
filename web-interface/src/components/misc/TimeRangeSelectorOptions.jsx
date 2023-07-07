import React from "react";

function TimeRangeSelectorOptions(props) {

  const show = props.show;

  if (!show) {
    return null;
  }

  return (
    <div className="timerange-selector-options">

      <div className="accordion" id="timerange-selector-accordion">
        <div className="accordion-item">
          <h2 className="accordion-header" id="trs-favorites-heading">
            <button className="accordion-button" type="button" data-bs-toggle="collapse" data-bs-target="#trs-favorites-collapse"
                    aria-expanded="true" aria-controls="trs-favorites-collapse">
              <i className="accordion-icon fa-solid fa-star"></i> Favorites
            </button>
          </h2>
          <div id="trs-favorites-collapse" className="accordion-collapse collapse show" aria-labelledby="trs-favorites-heading"
               data-bs-parent="#timerange-selector-accordion">
            <div className="accordion-body">
              <strong>WORK IN PROGRESS.</strong>
            </div>
          </div>
        </div>

        <div className="accordion-item">
          <h2 className="accordion-header" id="trs-presets-heading">
            <button className="accordion-button collapsed" type="button" data-bs-toggle="collapse"
                    data-bs-target="#trs-presets-collapse" aria-expanded="false" aria-controls="trs-presets-collapse">
              <i className="accordion-icon fa-regular fa-clock"></i> Presets
            </button>
          </h2>
          <div id="trs-presets-collapse" className="accordion-collapse collapse" aria-labelledby="trs-presets-heading"
               data-bs-parent="#timerange-selector-accordion">
            <div className="accordion-body">
              <div className="row">
                <div className="col-md-4">
                  <h3>Relative</h3>
                  <ul>
                    <li><a href="#">Last 1 Minute</a></li>
                    <li><a href="#">Last 5 Minutes</a></li>
                    <li><a href="#">Last 15 Minutes</a></li>
                    <li><a href="#">Last 30 Minutes</a></li>
                    <li><a href="#">Last 60 Minutes</a></li>
                    <li><a href="#">Last 4 Hours</a></li>
                    <li><a href="#">Last 24 Hours</a></li>
                    <li><a href="#">Last 2 Days</a></li>
                    <li><a href="#">Last 7 Days</a></li>
                  </ul>
                </div>
                <div className="col-md-4">
                  <h3>Quick Selectors</h3>
                  <ul>
                    <li><a href="#">Today</a></li>
                    <li><a href="#">Yesterday</a></li>
                    <li><a href="#">Week to date</a></li>
                    <li><a href="#">Month to date</a></li>
                  </ul>
                </div>
                <div className="col-md-4">
                  <h3>Other</h3>
                  <ul>
                    <li><a href="#">All time</a></li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="accordion-item">
          <h2 className="accordion-header" id="trs-range-heading">
            <button className="accordion-button collapsed" type="button" data-bs-toggle="collapse"
                    data-bs-target="#trs-range-collapse" aria-expanded="false" aria-controls="trs-range-collapse">
              <i className="accordion-icon fa-regular fa-calendar"></i> Time Range
            </button>
          </h2>
          <div id="trs-range-collapse" className="accordion-collapse collapse" aria-labelledby="trs-range-heading"
               data-bs-parent="#timerange-selector-accordion">
            <div className="accordion-body">
              <strong>WORK IN PROGRESS.</strong>
            </div>
          </div>
        </div>
      </div>

    </div>
  )

}

export default TimeRangeSelectorOptions;