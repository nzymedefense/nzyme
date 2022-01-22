import React from 'react'
import BanditForm from './BanditForm'
import { notify } from 'react-notify-toast'
import Routes from '../../../util/ApiRoutes'

class CreateBanditPage extends React.Component {
  _createBandit (e) {
    e.preventDefault()

    const self = this
    this.setState({ submitting: true })

    this.banditsService.createBandit(
      self.nameInput.current.value, self.descriptionInput.current.value,
      function () {
        self.setState({ submitting: false, submitted: true })
        notify.show('Bandit created.', 'success')
      },
      function () {
        self.setState({ submitting: false })
        notify.show('Could not create bandit. Please check nzyme log file.', 'error')
      }
    )
  }

  render () {
    return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <h1>Create Bandit</h1>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-9">
                        <BanditForm formHandler={this._createBandit} backLink={Routes.BANDITS.INDEX} submitName="Create Bandit" />
                    </div>

                    <div className="col-md-3">
                        <div className="alert alert-info">
                            <h3>Help</h3>
                            <p>
                                On this page, you create the initial details of a new bandit. Creating the definitions
                                that identify a bandit as a contact happens in a second step, on the bandit details page.
                            </p>
                        </div>
                    </div>
                </div>
            </div>
    )
  }
}

export default CreateBanditPage
