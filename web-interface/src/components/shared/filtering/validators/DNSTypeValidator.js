import {DNS_DATA_TYPES} from "../../../ethernet/dns/DNSDataTypes";

export default function validateDNSDataTypeValid(value) {
  return value && (DNS_DATA_TYPES.includes(value.trim()));
}