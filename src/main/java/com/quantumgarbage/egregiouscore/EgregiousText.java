package com.quantumgarbage.egregiouscore;

import net.swedz.tesseract.neoforge.compat.mi.tooltip.MICompatibleTranslatableTextEnum;

public enum EgregiousText implements MICompatibleTranslatableTextEnum {
// that's right, this is an enum with zero members!
;
  private final String englishText;

  EgregiousText(String englishText) {
    this.englishText = englishText;
  }

  @Override
  public String englishText() {
    return englishText;
  }

  @Override
  public String getTranslationKey() {
    return "text.%s.%s".formatted(EgregiousCore.ID, this.name().toLowerCase());
  }
}
