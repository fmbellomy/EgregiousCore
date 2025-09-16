package com.quantumgarbage.egregiouscore.compat.mi;

import com.quantumgarbage.egregiouscore.EgregiousMachines;
import net.swedz.tesseract.neoforge.compat.mi.hook.MIHookEntrypoint;
import net.swedz.tesseract.neoforge.compat.mi.hook.MIHookListener;
import net.swedz.tesseract.neoforge.compat.mi.hook.context.listener.MultiblockMachinesMIHookContext;

@MIHookEntrypoint
public class EgregiousMIHookListener implements MIHookListener {
  @Override
  public void multiblockMachines(MultiblockMachinesMIHookContext hook) {
    EgregiousMachines.multiblocks(hook);
  }
}
