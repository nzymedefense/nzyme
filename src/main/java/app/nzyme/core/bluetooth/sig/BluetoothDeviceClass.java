package app.nzyme.core.bluetooth.sig;

import jakarta.annotation.Nullable;

public class BluetoothDeviceClass {

    @Nullable
    private String majorServiceClass;

    @Nullable
    private String majorDeviceClass;

    @Nullable
    private String minorDeviceClass;

    public BluetoothDeviceClass(int classNumber) {
        this.majorServiceClass = decodeMajorServiceClass(classNumber);
        this.majorDeviceClass = decodeMajorDeviceClass(classNumber);
        this.minorDeviceClass = decodeMinorDeviceClass(classNumber, this.majorDeviceClass);
    }

    private String decodeMajorServiceClass(int classNumber) {
        StringBuilder services = new StringBuilder();
        if ((classNumber & 0x00200000) != 0) services.append("Limited Discoverable Mode, ");
        if ((classNumber & 0x00100000) != 0) services.append("Positioning, ");
        if ((classNumber & 0x00080000) != 0) services.append("Networking, ");
        if ((classNumber & 0x00040000) != 0) services.append("Rendering, ");
        if ((classNumber & 0x00020000) != 0) services.append("Capturing, ");
        if ((classNumber & 0x00010000) != 0) services.append("Object Transfer, ");
        if ((classNumber & 0x00008000) != 0) services.append("Audio, ");
        if ((classNumber & 0x00004000) != 0) services.append("Telephony, ");
        if ((classNumber & 0x00002000) != 0) services.append("Information, ");
        return services.toString().isEmpty() ? null : services.toString().replaceAll(", $", "");
    }

    private String decodeMajorDeviceClass(int classNumber) {
        int majorDeviceClass = (classNumber & 0x00001F00) >> 8;
        switch (majorDeviceClass) {
            case 1: return "Computer";
            case 2: return "Phone";
            case 3: return "LAN/Network Access Point";
            case 4: return "Audio/Video (AV)";
            case 5: return "Peripheral";
            case 6: return "Imaging";
            case 8: return "Wearable";
            case 9: return "Toy";
            case 10: return "Health";
            default: return "Miscellaneous";
        }
    }

    private String decodeMinorDeviceClass(int classNumber, String majorDeviceClass) {
        int minorDeviceClass = (classNumber & 0x000000FC) >> 2;
        switch (majorDeviceClass) {
            case "Computer":
                switch (minorDeviceClass) {
                    case 0: return "Uncategorized";
                    case 1: return "Desktop workstation";
                    case 2: return "Server-class computer";
                    case 3: return "Laptop";
                    case 4: return "Handheld PC/PDA";
                    case 5: return "Palm-size PC/PDA";
                    case 6: return "Wearable computer (watch size)";
                    default: return null;
                }
            case "Phone":
                switch (minorDeviceClass) {
                    case 0: return "Uncategorized";
                    case 1: return "Cellular";
                    case 2: return "Cordless";
                    case 3: return "Smartphone";
                    case 4: return "Wired modem or voice gateway";
                    case 5: return "Common ISDN access";
                    default: return null;
                }
            case "LAN/Network Access Point":
                switch (minorDeviceClass) {
                    case 0: return "Fully available";
                    case 8: return "1-17% utilized";
                    case 16: return "17-33% utilized";
                    case 24: return "33-50% utilized";
                    case 32: return "50-67% utilized";
                    case 40: return "67-83% utilized";
                    case 48: return "83-99% utilized";
                    case 56: return "No service available";
                    default: return null;
                }
            case "Audio/Video (AV)":
                switch (minorDeviceClass) {
                    case 0: return "Uncategorized";
                    case 1: return "Wearable Headset Device";
                    case 2: return "Hands-free Device";
                    case 3: return "Microphone";
                    case 4: return "Loudspeaker";
                    case 5: return "Headphones";
                    case 6: return "Portable Audio";
                    case 7: return "Car audio";
                    case 8: return "Set-top box";
                    case 9: return "HiFi Audio Device";
                    case 10: return "VCR";
                    case 11: return "Video Camera";
                    case 12: return "Camcorder";
                    case 13: return "Video Monitor";
                    case 14: return "Video Display and Loudspeaker";
                    case 15: return "Video Conferencing";
                    case 16: return "Gaming/Toy";
                    default: return null;
                }
            case "Peripheral":
                switch (minorDeviceClass) {
                    case 0: return "Not categorized";
                    case 1: return "Joystick";
                    case 2: return "Gamepad";
                    case 3: return "Remote control";
                    case 4: return "Sensing device";
                    case 5: return "Digitizer tablet";
                    case 6: return "Card Reader";
                    case 8: return "Keyboard";
                    case 16: return "Pointing device";
                    case 24: return "Combo keyboard/pointing device";
                    default: return null;
                }
            case "Imaging":
                switch (minorDeviceClass) {
                    case 0: return "Display";
                    case 1: return "Camera";
                    case 2: return "Scanner";
                    case 3: return "Printer";
                    default: return null;
                }
            case "Wearable":
                switch (minorDeviceClass) {
                    case 0: return "Wristwatch";
                    case 1: return "Pager";
                    case 2: return "Jacket";
                    case 3: return "Helmet";
                    case 4: return "Glasses";
                    default: return null;
                }
            case "Toy":
                switch (minorDeviceClass) {
                    case 0: return "Robot";
                    case 1: return "Vehicle";
                    case 2: return "Doll / Action Figure";
                    case 3: return "Controller";
                    case 4: return "Game";
                    default: return null;
                }
            case "Health":
                switch (minorDeviceClass) {
                    case 0: return "Undefined";
                    case 1: return "Blood Pressure Monitor";
                    case 2: return "Thermometer";
                    case 3: return "Weighing Scale";
                    case 4: return "Glucose Meter";
                    case 5: return "Pulse Oximeter";
                    case 6: return "Heart/Pulse Rate Monitor";
                    case 7: return "Health Data Display";
                    case 8: return "Step Counter";
                    case 9: return "Body Composition Analyzer";
                    default: return null;
                }
            default: return null;
        }
    }

    public String getMajorServiceClass() {
        return majorServiceClass;
    }

    public String getMajorDeviceClass() {
        return majorDeviceClass;
    }

    public String getMinorDeviceClass() {
        return minorDeviceClass;
    }

}
