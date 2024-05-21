pub fn tag(cts: &Vec<u8>, stc: &Vec<u8>) -> bool {

    if cts.len() < 3 { // TODO set properly
        return false;
    }

    return *cts.first().unwrap() == 0x04
        && (*cts.get(1).unwrap() == 0x01 || *cts.get(1).unwrap() == 0x02)

}