package com.quantumgarbage.egregiouscore.compat.mi;

import com.quantumgarbage.egregiouscore.EgregiousMachines;
import com.quantumgarbage.egregiouscore.compat.viewer.common.GasFuelCategory;
import net.swedz.tesseract.neoforge.compat.mi.hook.MIHookEntrypoint;
import net.swedz.tesseract.neoforge.compat.mi.hook.MIHookListener;
import net.swedz.tesseract.neoforge.compat.mi.hook.context.listener.MultiblockMachinesMIHookContext;
import net.swedz.tesseract.neoforge.compat.mi.hook.context.listener.ViewerSetupMIHookContext;

@MIHookEntrypoint
public class EgregiousMIHookListener implements MIHookListener {
  @Override
  public void multiblockMachines(MultiblockMachinesMIHookContext hook) {
    EgregiousMachines.multiblocks(hook);
  }

  @Override
  public void viewerSetup(ViewerSetupMIHookContext hook) {
    hook.register(new GasFuelCategory());
  }
}
