import RESTClient from '../util/RESTClient'

class EthernetService {

    findDNSStatistics(hours, setStatistics) {
        RESTClient.get('/ethernet/dns/statistics', {hours: hours}, function (response) {
            setStatistics(response.data);
        })
    }

}

export default EthernetService;

