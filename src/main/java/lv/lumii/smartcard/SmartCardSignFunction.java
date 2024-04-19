package lv.lumii.smartcard;

public interface SmartCardSignFunction {
    byte[] sign(byte[] message) throws Exception;
}
