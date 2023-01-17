package party;

import eu.cloudnetservice.driver.module.ModuleLifeCycle;
import eu.cloudnetservice.driver.module.ModuleTask;
import eu.cloudnetservice.driver.module.driver.DriverModule;

public class PartyModule extends DriverModule {

    @ModuleTask(event = ModuleLifeCycle.STARTED)
    private void onStart() { // Module successfully started
        //System.out.printf("Custom module %s started sucessfully", this.getClass().getName());
    }

    @ModuleTask(event = ModuleLifeCycle.LOADED)
    private void onLoad() { // Module is being loaded
        //System.out.printf("Trying to load custom module %s", this.getClass().getName());
    }

    @ModuleTask(event = ModuleLifeCycle.STOPPED)
    private void onStop() { // Module is being stopped
        //System.out.printf("Trying to disable custom module %s", this.getClass().getName());
    }

}
