export default function validatePortNumberValid(value) {
  const portNumber = Number(value.trim());
  return Number.isInteger(portNumber) && portNumber > 0 && portNumber <= 65535;
}