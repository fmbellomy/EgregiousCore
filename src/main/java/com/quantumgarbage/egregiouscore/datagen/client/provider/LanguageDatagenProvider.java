package com.quantumgarbage.egregiouscore.datagen.client.provider;

import com.quantumgarbage.egregiouscore.EgregiousCore;
import com.quantumgarbage.egregiouscore.EgregiousItems;
import com.quantumgarbage.egregiouscore.EgregiousText;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.swedz.tesseract.neoforge.datagen.mi.MIDatagenHooks;
import net.swedz.tesseract.neoforge.registry.holder.ItemHolder;

public class LanguageDatagenProvider extends LanguageProvider {
  public LanguageDatagenProvider(GatherDataEvent event) {
    super(event.getGenerator().getPackOutput(), EgregiousCore.ID, "en_us");
  }

  @Override
  protected void addTranslations() {
    for (EgregiousText text : EgregiousText.values()) {
      this.add(text.getTranslationKey(), text.englishText());
    }

    for (ItemHolder item : EgregiousItems.values()) {
      this.add(item.asItem(), item.identifier().englishName());
    }

    MIDatagenHooks.Client.withLanguageHook(this, EgregiousCore.ID);

    this.add("itemGroup.%s.%s".formatted(EgregiousCore.ID, EgregiousCore.ID), EgregiousCore.NAME);
  }
}
