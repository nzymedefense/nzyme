#[derive(Default, Debug)]
pub struct RemoteIdMessage {
    pub operator_license_id: Option<OperatorIdMessage>,
    pub uav_type: Option<UavType>,
    pub ids: Vec<UavIdSummary>,
    pub location_and_vector: Option<LocationVectorMessage>
}

#[derive(Debug)]
pub struct LocationVectorMessage {
    pub operational_status: OperationalStatus,
    pub height_type: HeightType,
    pub ground_track: u16,
    pub speed: f32,
    pub vertical_speed: f32,
    pub latitude: f64,
    pub longitude: f64,
    pub altitude_pressure: Option<f32>,
    pub altitude_geodetic: Option<f32>,
    pub height: Option<f32>,
    pub horizontal_accuracy: u8,
    pub vertical_accuracy: u8,
    pub barometer_accuracy: u8,
    pub speed_accuracy: u8
}

#[derive(Debug)]
pub struct BasicIdMessage {
    pub id_type: IdType,
    pub uav_type: UavType,
    pub uav_id: String
}

#[derive(Debug)]
pub struct OperatorIdMessage {
    pub operator_id: String
}

#[derive(Debug)]
pub struct UavIdSummary {
    pub id: String,
    pub id_type: IdType
}

pub struct InvalidTypeError;

#[derive(Debug)]
pub enum UavType {
    Undeclared,
    Aeroplane,
    MultirotorHelicopter,
    Gyroplane,
    Vtol,
    Ornithopter,
    Glider,
    Kite,
    FreeBalloon,
    CaptiveBalloon,
    Airship,
    UnpoweredFreeFall,
    Rocket,
    TetheredPowered,
    GroundObstacle,
    Other
}

impl TryFrom<u8> for UavType {
    type Error = InvalidTypeError;

    fn try_from(val: u8) -> Result<Self, Self::Error> {
        match val {
            0 => Ok(UavType::Undeclared),
            1 => Ok(UavType::Aeroplane),
            2 => Ok(UavType::MultirotorHelicopter),
            3 => Ok(UavType::Gyroplane),
            4 => Ok(UavType::Vtol),
            5 => Ok(UavType::Ornithopter),
            6 => Ok(UavType::Glider),
            7 => Ok(UavType::Kite),
            8 => Ok(UavType::FreeBalloon),
            9 => Ok(UavType::CaptiveBalloon),
            10 => Ok(UavType::Airship),
            11 => Ok(UavType::UnpoweredFreeFall),
            12 => Ok(UavType::Rocket),
            13 => Ok(UavType::TetheredPowered),
            14 => Ok(UavType::GroundObstacle),
            15 => Ok(UavType::Other),
            _ => Err(InvalidTypeError)
        }
    }
}

#[derive(Debug)]
pub enum IdType {
    AnsiCtaSerial,
    CaaRegistrationId,
    UtmAssignedUuid,
    SpecificSessionId
}

impl TryFrom<u8> for IdType {
    type Error = InvalidTypeError;

    fn try_from(val: u8) -> Result<Self, Self::Error> {
        match val {
            1 => Ok(IdType::AnsiCtaSerial),
            2 => Ok(IdType::CaaRegistrationId),
            3 => Ok(IdType::UtmAssignedUuid),
            4 => Ok(IdType::SpecificSessionId),
            _ => Err(InvalidTypeError)
        }
    }
}

#[derive(Debug)]
pub enum OperationalStatus {
    Undeclared,
    Ground,
    Airborne,
    Emergency,
    RemoteIdSystemFailure,
    Other
}

impl TryFrom<u8> for OperationalStatus {
    type Error = InvalidTypeError;

    fn try_from(val: u8) -> Result<Self, Self::Error> {
        match val {
            0 => Ok(OperationalStatus::Undeclared),
            1 => Ok(OperationalStatus::Ground),
            2 => Ok(OperationalStatus::Airborne),
            3 => Ok(OperationalStatus::Emergency),
            4 => Ok(OperationalStatus::RemoteIdSystemFailure),
            5..=15 => Ok(OperationalStatus::Other),
            _ => Err(InvalidTypeError)
        }
    }
}

#[derive(Debug)]
pub enum HeightType {
    AboveGround,
    AboveTakeoffLocation
}