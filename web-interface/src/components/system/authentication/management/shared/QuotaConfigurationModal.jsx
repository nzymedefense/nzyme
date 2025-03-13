import React, {useState} from "react";
import QuotaConfigurationModalSubmitButton from "./QuotaConfigurationModalSubmitButton";
import {notify} from "react-notify-toast";

export default function QuotaConfigurationModal(props) {

  const quota = props.quota;
  const onFinish = props.onFinish;
  const onSubmit = props.onSubmit;

  const [value, setValue] = useState(quota.quota == null ? 0 : quota.quota);
  const [quotaDisabled, setQuotaDisabled] = useState(quota.quota == null);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submittedSuccessfully, setSubmittedSuccessfully] = useState(false);

  const onSaved = (e) => {
    e.preventDefault();
    setIsSubmitting(true);

    onSubmit(quota.type, value, () => {
      setIsSubmitting(false);
      setSubmittedSuccessfully(true);
    }, () => {
      notify.show("Could not save quota.", 'error');
      setIsSubmitting(false);
      setSubmittedSuccessfully(false);
    })

    setSubmittedSuccessfully(true);
  }

  const formReady = () => {
    return quotaDisabled || (value != null && value >= 0);
  }

  return (
    <div className="modal" data-bs-keyboard="false"
         data-bs-backdrop="static" tabIndex="-1" id={"quota-config-" + quota.type}>
      <div className="modal-dialog">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">Configure Quota</h5>
          </div>

          <div className="modal-body">
            <label htmlFor={"quota-config-" + quota.type} className="form-label">
              Quota for {quota.type_human_readable}
            </label>

            <input type="number"
                   className="form-control"
                   autoComplete="off"
                   id={"quota-value-" + quota.type}
                   min={0}
                   disabled={quotaDisabled}
                   value={quotaDisabled ? "" : value} onChange={(e) => setValue(parseInt(e.target.value, 10))}/>

            <div className="mt-2">
              <input className="form-check-input"
                     id={"quota-config-disable-" + quota.type}
                     type="checkbox"
                     checked={quotaDisabled} onChange={() => { setQuotaDisabled(!quotaDisabled); setValue(null); }} />

              <label className="form-check-label ms-1" htmlFor={"quota-config-disable-" + quota.type}>
                No Quota
              </label>
            </div>
          </div>

          <div className="modal-footer">
            {submittedSuccessfully ? null :
                <button type="button"
                        className="btn btn-secondary"
                      data-bs-dismiss="modal"
                      disabled={isSubmitting}>
                Cancel
              </button>
            }

            <QuotaConfigurationModalSubmitButton onClick={onSaved}
                                                 onFinishedClick={onFinish}
                                                 disabled={!formReady}
                                                 submittedSuccessfully={submittedSuccessfully}
                                                 submitting={isSubmitting} />
          </div>
        </div>
      </div>
    </div>
  )

}