import React, { useState, useEffect } from 'react'
import LoadingSpinner from '../misc/LoadingSpinner'
import BanditsTableRow from './BanditsTableRow'
import Routes from '../../util/ApiRoutes'
import BanditsService from '../../services/BanditsService'

const banditsService = new BanditsService();

function fetchData(setBandits) {
  banditsService.findAll(setBandits);
}

function BanditsTable() {

  const [bandits, setBandits] = useState();

  useEffect(() => {
    fetchData(setBandits);
    const id = setInterval(() => fetchData(setBandits), 5000);
    return () => clearInterval(id);
  }, []);

  if (!bandits) {
    return <LoadingSpinner />
  }

  if (bandits.length === 0) {
    return (
              <div className="alert alert-info">
                  No bandits defined yet. <a href={Routes.BANDITS.NEW} className="text-dark"><u>Create a new bandit</u></a>
              </div>
    )
  }

  const self = this
  return (
          <div className="row">
              <div className="col-md-12">
                  <table className="table table-sm table-hover table-striped">
                      <thead>
                      <tr>
                          <th>Name</th>
                          <th>Active</th>
                          <th>Last Contact</th>
                          <th>Created</th>
                          <th>Last Updated</th>
                      </tr>
                      </thead>
                      <tbody>
                      {Object.keys(bandits).map(function (key, i) {
                        return <BanditsTableRow key={'bandit-' + i} bandit={bandits[key]} />
                      })}
                      </tbody>
                  </table>
              </div>
          </div>
  )
  
}

export default BanditsTable
