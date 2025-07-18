import {FILTER_TYPE} from "../../../shared/filtering/Filters";

export const ARP_FILTER_FIELDS = {
  operation: { title: "ARP Operation", type: FILTER_TYPE.STRING },
  ethernet_source_mac: { title: "Ethernet Source MAC", type: FILTER_TYPE.STRING },
  ethernet_destination_mac: { title: "Ethernet Destination MAC", type: FILTER_TYPE.STRING },
  arp_sender_mac: { title: "Sender MAC", type: FILTER_TYPE.STRING },
  arp_sender_address: { title: "Sender Address", type: FILTER_TYPE.IP_ADDRESS },
  arp_target_mac: { title: "Target MAC", type: FILTER_TYPE.STRING },
  arp_target_address: { title: "Target Address", type: FILTER_TYPE.IP_ADDRESS }
}