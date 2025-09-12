package com.quantumgarbage.egregiouscore.multis;

import aztech.modern_industrialization.machines.init.MachineRegistrationHelper;

public class Multiblocks {
  public static void init() {
    MachineRegistrationHelper.registerMachine(
        "Ore Drilling Plannt", "ore_drilling_plant", OreDrillingPlant::new);
  }
}
