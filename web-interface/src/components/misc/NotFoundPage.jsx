  import React from 'react'
  import usePageTitle from "../../util/UsePageTitle";

  export default function NotFoundPage() {

    usePageTitle("Page not found");

    return (
      <div>
        <div className="row">
          <div className="col-12">
            <h1>404 - Not found!</h1>
          </div>
        </div>

        <div className="row mt-2">
          <div className="col-12">
            <div className="alert alert-danger mb-0">
              <strong>Page not found.</strong>
            </div>
          </div>
        </div>
      </div>
    )

  }