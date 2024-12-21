export default function validateMACAddressValid(value) {
  return /^[0-9A-F]{2}(:[0-9A-F]{2}){5}$/.test(value)
}