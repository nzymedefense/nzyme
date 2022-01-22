import React, { useState, useEffect } from 'react'
import { useParams } from 'react-router-dom'

import BanditForm from './BanditForm'
import Routes from '../../../util/ApiRoutes'
import LoadingSpinner from '../../misc/LoadingSpinner'
import { notify } from 'react-notify-toast'
import BanditsService from '../../../services/BanditsService'

function _editBandit (e) {
  e.preventDefault()

  const self = this
  this.setState({ submitting: true })

  this.banditsService.updateBandit(
    self.state.banditId, self.nameInput.current.value, self.descriptionInput.current.value,
    function () {
      self.setState({ submitting: false, submitted: true })
      notify.show('Bandit updated.', 'success')
    },
    function () {
      self.setState({ submitting: false })
      notify.show('Could not update bandit. Please check nzyme log file.', 'error')
    }
  )
}

const banditsService = new BanditsService();

function EditBanditPage() {

  const { banditId } = useParams();
  const [bandit, setBandit] = useState(null);

  useEffect(() => {
    banditsService.findOne(banditId, setBandit);
  }, [banditId])

  if (!bandit) {
    return <LoadingSpinner />
  }

  return (
          <div>
              <div className="row">
                  <div className="col-md-12">
                      <nav aria-label="breadcrumb">
                          <ol className="breadcrumb">
                              <li className="breadcrumb-item">
                                  <a href={Routes.BANDITS.INDEX}>Bandits</a>
                              </li>
                              <li className="breadcrumb-item" aria-current="page">
                                  <a href={Routes.BANDITS.SHOW(bandit.uuid)}>{bandit.name}</a>
                              </li>
                              <li className="breadcrumb-item active" aria-current="page">
                                  Edit
                              </li>
                          </ol>
                      </nav>
                  </div>
              </div>

              <div className="row">
                  <div className="col-md-12">
                      <h1>Edit Bandit <em>{bandit.name}</em></h1>
                  </div>
              </div>

              <div className="row">
                  <div className="col-md-12">
                      <BanditForm formHandler={_editBandit}
                                  backLink={Routes.BANDITS.SHOW(bandit.uuid)}
                                  bandit={bandit}
                                  submitName="Edit Bandit" />
                  </div>
              </div>
          </div>
  )

}

export default EditBanditPage
