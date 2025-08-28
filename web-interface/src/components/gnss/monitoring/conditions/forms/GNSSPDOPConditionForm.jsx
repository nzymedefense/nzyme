import React, {useState} from "react";

export default function GNSSPDOPConditionForm(props) {

  const onConditionAdded = props.onConditionAdded;

  const [pdop, setPdop] = useState(3);

  const formIsReady = () => {
    return pdop !== null && pdop > 0
  }

  const format = () => {
    return { pdop: pdop }
  }

  return (
      <React.Fragment>
        <p>
          Condition triggers if the recorded GNSS PDOP (Position Dilution of Precision) is greater than the defined
          threshold. For example, you could configure this condition to trigger if a recorded
          GNSS PDOP is larger than 6, representing bad positioning precision.
        </p>

        <label htmlFor="condition_pdop" className="form-label">Maximum PDOP</label>
        <input type="number"
               className="form-control"
               id="condition_pdop"
               min={1}
               value={pdop}
               onChange={(e) => {
                 setPdop(parseInt(e.target.value, 10))
               }}/>

        <div className="mt-2">
          <button className="btn btn-primary"
                  disabled={!formIsReady()}
                  onClick={(e) => { e.preventDefault(); onConditionAdded(format()) }}>
            Add Condition
          </button>
        </div>
      </React.Fragment>
  )

}