import React from 'react'
import LoadingSpinner from '../../misc/LoadingSpinner'

function PGPKeyTable (props) {
  const keys = props.keys

  if (!keys) {
    return <LoadingSpinner />
  }

  return (
        <table className="table table-sm table-hover table-striped">
            <thead>
            <tr>
                <th>Leader Node</th>
                <th>Key Fingerprint</th>
                <th>Generated at</th>
            </tr>
            </thead>
            <tbody>
            {Object.keys(keys.sort((a, b) => a.node.localeCompare(b.node))).map(function (key, i) {
              return (
                    <tr key={'ppgkey-' + i}>
                        <td>{keys[i].node}</td>
                        <td>{keys[i].fingerprint.match(/.{1,2}/g).join(' ')}</td>
                        <td>{keys[i].created_at}</td>
                    </tr>
              )
            })}
            </tbody>
        </table>
  )
}

export default PGPKeyTable
