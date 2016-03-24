package adf.launcher.connect;

import adf.agent.office.OfficeAmbulance;
import adf.component.control.ControlAmbulance;
import adf.component.AbstractLoader;
import adf.launcher.ConfigKey;
import adf.launcher.dummy.control.DummyControlAmbulance;
import rescuecore2.components.ComponentConnectionException;
import rescuecore2.components.ComponentLauncher;
import rescuecore2.config.Config;
import rescuecore2.connection.ConnectionException;

public class ConnectorAmbulanceCentre implements Connector {
	@Override
	public void connect(ComponentLauncher launcher, Config config, AbstractLoader loader)
	{
		int count = config.getIntValue(ConfigKey.KEY_AMBULANCE_CENTRE_COUNT, 0);
		int connected = 0;

		if (count == 0) {
			return;
		}

		try {
			for (int i = 0; i != count; ++i) {
				ControlAmbulance controlAmbulance;
				if (loader.getControlAmbulance() == null)
				{
					System.out.println("[ERROR ] Cannot Load AmbulanceCentre Control !!");
					controlAmbulance = new DummyControlAmbulance();
				}
				else
				{
					controlAmbulance = loader.getControlAmbulance();
				}
				boolean isPrecompute = config.getBooleanValue(ConfigKey.KEY_PRECOMPUTE, false);
				launcher.connect(new OfficeAmbulance(controlAmbulance, isPrecompute));
				//System.out.println(name);
				connected++;
			}
		}
		catch (ComponentConnectionException | InterruptedException | ConnectionException e) {
			//e.printStackTrace();
			//System.out.println("[ERROR ] Cannot Load AmbulanceCentre Control !!");
		}
		System.out.println("[FINISH] Connect AmbulanceCentre (success:" + connected + ")");
	}
}
