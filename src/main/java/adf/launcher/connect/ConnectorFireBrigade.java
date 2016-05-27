package adf.launcher.connect;

import adf.agent.platoon.PlatoonFire;
import adf.component.tactics.TacticsFire;
import adf.component.AbstractLoader;
import adf.launcher.ConfigKey;
import adf.launcher.dummy.tactics.DummyTacticsFire;
import rescuecore2.components.ComponentConnectionException;
import rescuecore2.components.ComponentLauncher;
import rescuecore2.config.Config;
import rescuecore2.connection.ConnectionException;

public class ConnectorFireBrigade implements Connector {
	@Override
	public void connect(ComponentLauncher launcher, Config config, AbstractLoader loader)
	{
		int count = config.getIntValue(ConfigKey.KEY_FIRE_BRIGADE_COUNT, 0);
		int connected = 0;

		if (count == 0) {
			return;
		}

		try {
			for (int i = 0; i != count; ++i) {
				TacticsFire tacticsFire;
				if (loader.getTacticsFire() == null)
				{
					System.out.println("[ERROR ] Cannot Load FireBrigade Tactics !!");
					tacticsFire = new DummyTacticsFire();
				}
				else
				{
					tacticsFire = loader.getTacticsFire();
				}
				boolean isPrecompute = config.getBooleanValue(ConfigKey.KEY_PRECOMPUTE, false);
				launcher.connect(new PlatoonFire(tacticsFire, config.getValue(ConfigKey.KEY_MODULE_CONFIG_FILE_NAME), isPrecompute));
				//System.out.println(name);
				connected++;
			}
		}
		catch (ComponentConnectionException | InterruptedException | ConnectionException e) {
			//e.printStackTrace();
			//System.out.println("[ERROR ] Cannot Load FireBrigade Tactics !!");
		}
		System.out.println("[FINISH] Connect FireBrigade (success:" + connected + ")");
	}
}
