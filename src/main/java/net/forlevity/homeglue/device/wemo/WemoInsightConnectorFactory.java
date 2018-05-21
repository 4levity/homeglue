package net.forlevity.homeglue.device.wemo;

public interface WemoInsightConnectorFactory {
    WemoInsightConnector create(String hostAddress, int port);
}
