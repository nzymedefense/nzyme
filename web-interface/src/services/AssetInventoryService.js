import RESTClient from '../util/RESTClient'

class AssetInventoryService {
  findAllDot11Assets () {
    const self = this
    RESTClient.get('/asset-inventory', {}, function (response) {
      self.setState({ ssids: response.data.ssids, ssids_csv: response.data.ssids_csv, bssids_csv: response.data.bssids_csv })
    })
  }
}

export default AssetInventoryService
