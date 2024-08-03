package app.nzyme.core.ethernet.dns.filters;

import app.nzyme.core.util.filters.FilterOperator;
import app.nzyme.core.util.filters.SqlFilterProvider;

public class DnsFilters implements SqlFilterProvider {

    @Override
    public String buildSql(String binding, String fieldName, FilterOperator operator) {
        switch (fieldName) {
            case "query_value":
                return "dns_type = 'response' OR (dns_type = 'query' AND (" + stringMatch(binding, "data_value", operator) + "))";
            case "query_type":
                return "dns_type = 'response' OR (dns_type = 'query' AND (" + stringMatch(binding, "data_type", operator) + "))";
            case "query_etld":
                return "dns_type = 'response' OR (dns_type = 'query' AND (" + stringMatch(binding, "data_value_etld", operator) + "))";
            case "client_address":
                return ipAddressMatch(binding, "client_address", operator);
            case "server_address":
                return ipAddressMatch(binding, "server_address", operator);
            case "server_port":
                return numericMatch(binding, "server_port", operator);
            default:
                throw new RuntimeException("Unknown field name [" + fieldName + "].");
        }
    }

    private static String stringMatch(String binding, String fieldName, FilterOperator operator) {
        switch (operator) {
            case EQUALS:
                return fieldName + " = :" + binding;
            case NOT_EQUALS:
                return fieldName + " <> :" + binding;
            case REGEX_MATCH:
                return fieldName + " ~ :" + binding;
            case NOT_REGEX_MATCH:
                return fieldName + " !~ :" + binding;
            default:
                throw new RuntimeException("Invalid operator [" + operator + "] for string field [" + fieldName + "].");
        }
    }

    private static String numericMatch(String binding, String fieldName, FilterOperator operator) {
        switch (operator) {
            case EQUALS_NUMERIC:
                return fieldName + " = :" + binding;
            case NOT_EQUALS_NUMERIC:
                return fieldName + " <> :" + binding;
            case GREATER_THAN:
                return fieldName + " > :" + binding;
            case SMALLER_THAN:
                return fieldName + " < :" + binding;
            default:
                throw new RuntimeException("Invalid operator [" + operator + "] for numeric field [" + fieldName + "].");
        }
    }

    private static String ipAddressMatch(String binding, String fieldName, FilterOperator operator) {
        switch (operator) {
            case EQUALS:
                return fieldName + " = :" + binding + "::inet";
            case NOT_EQUALS:
                return fieldName + " <> :" + binding + "::inet";
            case REGEX_MATCH:
                return fieldName + " ~ :" + binding;
            case NOT_REGEX_MATCH:
                return fieldName + " !~ :" + binding;
            case IN_CIDR:
                return fieldName + " <<= :" + binding + "::cidr";
            case NOT_IN_CIDR:
                return fieldName + " <<| :" + binding + "::cidr";
            case IS_PRIVATE:
                return "(" + fieldName + " <<= '10.0.0.0/8'::cidr OR "
                        + fieldName + " <<= '172.16.0.0/12'::cidr OR "
                        + fieldName + " <<= '192.168.0.0/16'::cidr)";
            case IS_NOT_PRIVATE:
                return "NOT (" + fieldName + " <<= '10.0.0.0/8'::cidr OR "
                        + fieldName + " <<= '172.16.0.0/12'::cidr OR "
                        + fieldName + " <<= '192.168.0.0/16'::cidr)";
            default:
                throw new RuntimeException("Invalid operator [" + operator + "] for IP address " +
                        "field [" + fieldName + "].");
        }
    }

}
