import React, {useEffect, useState} from "react";
import LoadingSpinner from "../../misc/LoadingSpinner";
import Dot11Service from "../../../services/Dot11Service";

const dot11Service = new Dot11Service();

function Dot11BanditsTable() {

  const [bandits, setBandits] = useState(null);

  useEffect(() => {
    dot11Service.findSupportedBandits(setBandits);
  }, [])

  if (!bandits) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <table className="table table-sm table-hover table-striped mb-0">
          <thead>
          <tr>
            <th>Name</th>
            <th>Description</th>
          </tr>
          </thead>
          <tbody>
          {bandits.map(function(bandit, i) {
            return (
                <tr key={"banditdef-" + i}>
                  <td>{bandit.name}</td>
                  <td>{bandit.description}</td>
                </tr>
            )
          })}
          </tbody>
        </table>
      </React.Fragment>
  )

}

export default Dot11BanditsTable;