import numeral from "numeral";

function byteAverageToMbit(byte) {
    const mbit = byte*8/10/1024/1024;

    if (mbit > 1000) {
        return numeral(mbit/1024).format('0.0')  + " Gbit/sec";
    } else {
        return numeral(mbit).format('0.0') + " Mbit/sec";
    }
}

export default byteAverageToMbit;